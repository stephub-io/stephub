import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";

import {
  NotificationService,
  ROUTE_ANIMATIONS_ELEMENTS,
} from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { ExecutionService } from "../execution.service";
import { BehaviorSubject } from "rxjs";
import { ActivatedRoute, Router } from "@angular/router";
import { Workspace } from "../workspace/workspace.model";
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from "@angular/forms";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";
import {
  ExecutionStart,
  ParallelizationMode,
  ScenariosExecutionInstruction,
  SessionSettings,
} from "../execution.model";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import {
  VariableDialogComponent,
  VariableDialogMode,
} from "../variable-dialog/variable-dialog.component";
import {
  FieldError,
  ServerError,
} from "../../../core/server-error/server-error.model";
import { SuggestOption } from "../../../util/auto-suggest";

@Component({
  selector: "sh-execution-new",
  templateUrl: "./execution-new.component.html",
  styleUrls: ["./execution-new.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionNewComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  wid: string;
  workspace$: BehaviorSubject<Workspace> = new BehaviorSubject<Workspace>(null);
  workspace: Workspace;

  selectionFormGroup: FormGroup;
  settingsFormGroup: FormGroup;
  varFormGroup: FormGroup;

  faExecutions = faExecutions;
  selectionType = SelectionType.all;
  variableKeys$ = new BehaviorSubject<string[]>([]);
  executionStart: ExecutionStart;

  fieldErrors$ = new BehaviorSubject<FieldError[]>(null);
  filterScenarios: string[] = [];
  filterFeatures: string[] = [];
  filterTags: string[] = [];

  regexValidator = (control: FormControl): { [key: string]: any } | null => {
    try {
      if (control.value && control.value.length > 0) {
        new RegExp(control.value);
      }
      return null;
    } catch (e) {
      return { invalidRegex: e.message };
    }
  };

  scenarioAutoCompleteSource: (value: string) => SuggestOption[] = (value) =>
    autoCompleteFilter(allScenarios(this.workspace), value);
  featureAutoCompleteSource: (value: string) => SuggestOption[] = (value) =>
    autoCompleteFilter(
      this.workspace.features.map((f) => escapeRegExp(f.name)),
      value
    );
  tagsAutoCompleteSource: (value: string) => SuggestOption[] = (value) =>
    autoCompleteFilter(allTags(this.workspace), value);

  constructor(
    private workspaceService: WorkspaceService,
    private executionService: ExecutionService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute,
    private _formBuilder: FormBuilder,
    private titleService: Title,
    private dialog: MatDialog
  ) {
    this.route.parent.parent.params.subscribe((params) => {
      this.wid = params.wid;
    });
    this.route.queryParams.subscribe((params) => {
      if (params["scenario"]) {
        this.selectionType = SelectionType.filter_scenario;
        this.filterScenarios.push(escapeRegExp(params["scenario"]));
      } else if (params["feature"]) {
        this.selectionType = SelectionType.filter_feature;
        this.filterFeatures.push(escapeRegExp(params["feature"]));
      }
    });
  }

  ngOnInit() {
    this.selectionFormGroup = this._formBuilder.group({
      type: ["", Validators.required],
    });
    this.workspace$.pipe();
    this.workspaceService.get(this.wid).subscribe((workspace) => {
      this.workspace$.next(workspace);
      this.workspace = workspace;
      this.varFormGroup = this._formBuilder.group({});
      this.settingsFormGroup = this._formBuilder.group({
        parallelizationMode: ["", Validators.required],
        parallelSessionCount: [
          "",
          [Validators.min(1), Validators.max(this.maxParallelSessionCount())],
        ],
      });
      if (this.workspaceHasErrors()) {
        this.varFormGroup.disable();
        this.selectionFormGroup.disable();
        this.settingsFormGroup.disable();
      }
      this.variableKeys$.next(this.keys(workspace.variables));
      this.setTitle(workspace);
      return workspace;
    });
    this.executionStart = {
      sessionSettings: {
        variables: {},
        parallelizationMode: ParallelizationMode.scenario,
      } as SessionSettings,
      parallelSessionCount: 1,
    } as ExecutionStart;
  }

  private setTitle(workspace: Workspace) {
    this.titleService.setTitle(`${workspace.name} - ${env.appName}`);
  }

  keys(obj: Object): string[] {
    return Object.keys(obj);
  }

  setVariable(key: string) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;

    dialogConfig.data = {
      mode: VariableDialogMode.value,
      name: key,
      variable: this.workspace.variables[key],
      value: this.isVariableSet(key)
        ? this.executionStart.sessionSettings.variables[key]
        : this.workspace.variables[key].defaultValue,
    };
    dialogConfig.width = "50%";
    this.dialog
      .open(VariableDialogComponent, dialogConfig)
      .afterClosed()
      .subscribe((data) => {
        if (data) {
          this.executionStart.sessionSettings.variables[key] = data.value;
        }
        // Refresh table
        this.variableKeys$.next(this.keys(this.workspace.variables));
      });
  }

  eraseVariable(key: string) {
    delete this.executionStart.sessionSettings.variables[key];
  }

  isVariableSet(key: string) {
    return this.executionStart.sessionSettings.variables.hasOwnProperty(key);
  }

  maxParallelSessionCount() {
    let count = 0;
    if (
      this.executionStart.sessionSettings.parallelizationMode ==
      ParallelizationMode.scenario
    ) {
      this.workspace.features.forEach(
        (feature) => (count += feature.scenarios.length)
      );
    } else {
      count = this.workspace.features.length;
    }
    return count;
  }

  changedParallelizationMode() {
    this.executionStart.parallelSessionCount = Math.min(
      this.executionStart.parallelSessionCount,
      this.maxParallelSessionCount()
    );
  }

  start() {
    switch (this.selectionType) {
      case SelectionType.all:
        this.executionStart.instruction = {
          type: "scenarios",
          filter: {
            type: "all",
          },
        } as ScenariosExecutionInstruction;
        break;
      case SelectionType.filter_scenario:
        this.executionStart.instruction = {
          type: "scenarios",
          filter: {
            type: "by-scenario-name",
            patterns: this.filterScenarios,
          },
        } as ScenariosExecutionInstruction;
        break;
      case SelectionType.filter_feature:
        this.executionStart.instruction = {
          type: "scenarios",
          filter: {
            type: "by-feature-name",
            patterns: this.filterFeatures,
          },
        } as ScenariosExecutionInstruction;
        break;
      case SelectionType.filter_tag:
        this.executionStart.instruction = {
          type: "scenarios",
          filter: {
            type: "by-tag",
            patterns: this.filterTags,
          },
        } as ScenariosExecutionInstruction;
        break;
    }
    this.fieldErrors$.next(null);
    this.executionService.start(this.wid, this.executionStart).subscribe(
      (execution) => {
        this.notificationService.success("Execution initiated successfully");
        this.router.navigate(["../", execution.id], { relativeTo: this.route });
      },
      (error) => {
        if (error instanceof ServerError) {
          this.fieldErrors$.next(error.errors);
        }
        throw error;
      }
    );
  }

  workspaceHasErrors() {
    return this.workspace.errors?.length > 0;
  }

  invalidSelection() {
    switch (this.selectionType) {
      case SelectionType.filter_scenario:
        return this.filterScenarios.length == 0;
      case SelectionType.filter_feature:
        return this.filterFeatures.length == 0;
      case SelectionType.filter_tag:
        return this.filterTags.length == 0;
    }
    return false;
  }
}

enum SelectionType {
  all = "all",
  filter_tag = "filter-tag",
  filter_scenario = "filter-scenario",
  filter_feature = "filter-feature",
}

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function allScenarios(workspace: Workspace): string[] {
  const scenarios = [];
  workspace.features.forEach((f) =>
    f.scenarios.forEach((s) => scenarios.push(escapeRegExp(s.name)))
  );
  return [...new Set(scenarios)];
}

function allTags(workspace: Workspace): string[] {
  const tags: string[] = [];
  workspace.features.forEach((f) => {
    if (f.tags) {
      f.tags.forEach((t) => tags.push(escapeRegExp(t)));
    }
    f.scenarios.forEach((s) => {
      if (s.tags) {
        s.tags.forEach((t) => tags.push(escapeRegExp(t)));
      }
    });
  });
  return [...new Set(tags)];
}

function autoCompleteFilter(options: string[], value: string): SuggestOption[] {
  const filterValue = value.toLowerCase();
  return options
    .filter((option) => option.toLowerCase().includes(filterValue))
    .map((s) => {
      return {
        value: s,
        view: s,
      } as SuggestOption;
    });
}
