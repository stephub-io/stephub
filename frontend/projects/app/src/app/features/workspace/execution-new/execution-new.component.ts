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
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
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
import { ServerError } from "../../../core/server-error/server-error.model";

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

  fieldErrors$ = new BehaviorSubject(null);

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
}

enum SelectionType {
  all = "all",
}
