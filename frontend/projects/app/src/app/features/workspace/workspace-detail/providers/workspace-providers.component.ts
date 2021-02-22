import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import {
  ProviderRemoteConfig,
  ProviderSpec,
  StepsCollection,
  Workspace,
} from "../../workspace/workspace.model";
import { FormControl, Validators } from "@angular/forms";
import { BehaviorSubject, Observable, Subscriber } from "rxjs";
import { map, startWith } from "rxjs/operators";
import { ProviderService } from "../../provider/provider.service";
import {
  FieldError,
  ServerError,
} from "../../../../core/server-error/server-error.model";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { isNullable } from "../../../../shared/json-schema-view/json--schema-view.component";
import { ProviderInfo } from "../../provider/provider.model";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { JsonEditDialogComponent } from "../../json-edit-dialog/json-edit-dialog.component";
import { StepSpec } from "../../step/step.model";
import { StepBrowserDialogComponent } from "../../step/step-browser-dialog/step-browser-dialog.component";

@Component({
  selector: "sh-workspace-providers",
  templateUrl: "./workspace-providers.component.html",
  styleUrls: ["./workspace-providers.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceProvidersComponent implements OnInit {
  @Input() workspace: Workspace;
  @Input() editMode = false;
  @Input() errors: FieldError[];

  autoCompleteNames$: Observable<string[]>;

  registeredProviders$ = new BehaviorSubject<ProviderInfo[]>(null);

  providerInfoHolders$: Observable<BehaviorSubject<ProviderInfoHolder>>[] = [];
  private providerInfoSubscribers$: Subscriber<
    BehaviorSubject<ProviderInfoHolder>
  >[] = [];

  nameControlFactory: (value: any) => FormControl = (givenValue) => {
    let control = new FormControl(givenValue, Validators.required);
    this.autoCompleteNames$ = control.valueChanges.pipe(
      startWith(givenValue),
      map((value) => this.filterAutoCompleteNames(value))
    );
    return control;
  };
  autoCompleteVersions$: Observable<string[]>;
  urlValidator = Validators.pattern(
    /^(([^:/?#]+):)?(\/\/([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?/
  );

  versionControlFactoryByName(providerName: string) {
    return (givenValue) => {
      let control = new FormControl(givenValue);
      this.autoCompleteVersions$ = control.valueChanges.pipe(
        startWith(givenValue),
        map((value) => this.filterAutoCompleteVersions(providerName, value))
      );
      return control;
    };
  }

  constructor(
    private providerService: ProviderService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.providerService.getRegistered().subscribe((providers) => {
      this.registeredProviders$.next(providers);
      return providers;
    });
    this.workspace.providers.forEach((p, i) => {
      this.buildNewHolder(i, p);
    });
  }

  private buildNewHolder(i: number, p: ProviderSpec) {
    this.providerInfoSubscribers$[i] = null;
    this.providerInfoHolders$[i] = new Observable<
      BehaviorSubject<ProviderInfoHolder>
    >((observer) => {
      this.providerInfoSubscribers$[i] = observer;
      observer.next(this.createHolderObserver(p));
    });
  }

  private refreshProviderInfo(fi: number) {
    this.providerInfoSubscribers$[fi].next(
      this.createHolderObserver(this.workspace.providers[fi])
    );
  }

  private createHolderObserver(
    spec: ProviderSpec
  ): BehaviorSubject<ProviderInfoHolder> {
    const holder = new BehaviorSubject(new ProviderInfoHolder(true));
    this.providerService.getProviderInfo(spec).subscribe(
      (info) => {
        holder.next(new ProviderInfoHolder(false, info));
        return info;
      },
      (error) => {
        if (error instanceof ServerError) {
          holder.next(
            new ProviderInfoHolder(
              false,
              null,
              true,
              (error as ServerError).errors || []
            )
          );
        } else {
          holder.next(new ProviderInfoHolder(false, null, true));
        }
        console.error(
          "Failed to lookup provider info due to: " + error.message,
          spec
        );
      }
    );
    return holder;
  }

  deleteProvider(fi: number) {
    this.workspace.providers.splice(fi, 1);
    this.workspace.providers = [...this.workspace.providers];
    this.providerInfoSubscribers$.splice(fi, 1);
    this.providerInfoHolders$.splice(fi, 1);
  }

  addProvider() {
    const i = this.workspace.providers.length;
    this.workspace.providers[i] = {
      name: "",
      version: null,
      options: {},
    } as ProviderSpec;
    this.buildNewHolder(i, this.workspace.providers[i]);
  }

  private filterAutoCompleteNames(value: string) {
    return filter(
      value,
      this.registeredProviders$.value
        ? this.registeredProviders$.value.map((p) => p.name)
        : []
    );
  }

  private filterAutoCompleteVersions(name: string, value: string) {
    if (name && this.registeredProviders$.value) {
      let candidates = [];
      filter(
        value,
        this.registeredProviders$.value
          .filter((p) => p.name == name && p.version)
          .map((p) => p.version)
      ).forEach((c) => candidates.push(c, ">" + c, ">=" + c));
      return candidates;
    }
    return [];
  }

  changeRemoteConfig(provider: ProviderSpec, event: MatSlideToggleChange) {
    if (event.checked) {
      provider.remoteConfig = {} as ProviderRemoteConfig;
    } else {
      provider.remoteConfig = null;
    }
  }

  optionsAdapter(fc: FormControl): OptionsAdapter {
    return new OptionsAdapter(fc);
  }

  nullSchema(schema: any): boolean {
    return isNullable(schema);
  }

  concat(errors1: FieldError[], errors2: FieldError[]) {
    return [...errors1, ...errors2];
  }

  editProviderOptions(spec: ProviderSpec, infoHolder: ProviderInfoHolder) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = false;
    dialogConfig.data = {
      value: spec.options,
      schema: infoHolder.info?.optionsSchema,
    };
    dialogConfig.width = "50%";
    dialogConfig.minHeight = "50%";

    this.dialog
      .open(JsonEditDialogComponent, dialogConfig)
      .afterClosed()
      .subscribe((data) => {
        if (data) {
          spec.options = data.value;
        }
      });
  }

  setRemoteConfig(fi: number, config: object) {
    this.workspace.providers[fi].remoteConfig = {
      ...this.workspace.providers[fi].remoteConfig,
      ...config,
    };
    this.refreshProviderInfo(fi);
  }

  setVersion(fi: number, version: string) {
    this.workspace.providers[fi].version = version;
    this.refreshProviderInfo(fi);
  }

  setName(fi: number, name: string) {
    this.workspace.providers[fi].name = name;
    this.refreshProviderInfo(fi);
  }

  showSteps(provider: ProviderSpec, steps: StepSpec[]) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = false;
    dialogConfig.data = {
      stepsCollection: {
        [provider.name]: steps,
      } as StepsCollection,
    };
    dialogConfig.width = "80%";
    dialogConfig.minHeight = "80%";

    this.dialog.open(StepBrowserDialogComponent, dialogConfig);
  }
}

class OptionsAdapter {
  private fc: FormControl;

  constructor(fc: FormControl) {
    this.fc = fc;
  }

  get aceValue() {
    return JSON.stringify(this.fc.value, null, 4);
  }

  set aceValue(value: string) {
    try {
      this.fc.updateValueAndValidity({
        emitEvent: true,
      });
      this.fc.setValue(JSON.parse(value));
    } catch (e) {
      this.fc.setErrors({
        json: e.message,
      });
    }
  }
}

class ProviderInfoHolder {
  loading: boolean = true;
  info: ProviderInfo = null;
  error: boolean = false;
  errors: FieldError[] = [];

  constructor(
    loading: boolean,
    info: ProviderInfo = null,
    error: boolean = false,
    errors: FieldError[] = []
  ) {
    this.loading = loading;
    this.info = info;
    this.error = error;
    this.errors = errors;
  }
}

function filter(value: string, options: string[]): string[] {
  const filterValue = value ? value.toLowerCase() : "";
  return options
    .filter((option) => option.toLowerCase().includes(filterValue))
    .sort();
}
