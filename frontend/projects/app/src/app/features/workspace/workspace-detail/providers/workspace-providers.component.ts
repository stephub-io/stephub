import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import {
  ProviderRemoteConfig,
  ProviderSpec,
  Workspace,
} from "../../workspace/workspace.model";
import { FormControl, Validators } from "@angular/forms";
import { BehaviorSubject, Observable } from "rxjs";
import { map, startWith } from "rxjs/operators";
import { ProviderService } from "../../provider/provider.service";
import { RegisteredProvider } from "../../provider/provider.model";
import { FieldError } from "../../../../core/server-error/server-error.model";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";

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

  registeredProviders$ = new BehaviorSubject<RegisteredProvider[]>(null);

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

  constructor(private providerService: ProviderService) {}

  ngOnInit() {
    this.providerService.getRegistered().subscribe((providers) => {
      this.registeredProviders$.next(providers);
      return providers;
    });
  }

  deleteProvider(fi: number) {
    this.workspace.providers.splice(fi, 1);
    this.workspace.providers = [...this.workspace.providers];
  }

  addProvider() {
    this.workspace.providers.push({
      name: "",
      version: null,
    } as ProviderSpec);
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
}

function filter(value: string, options: string[]): string[] {
  const filterValue = value ? value.toLowerCase() : "";
  return options
    .filter((option) => option.toLowerCase().includes(filterValue))
    .sort();
}
