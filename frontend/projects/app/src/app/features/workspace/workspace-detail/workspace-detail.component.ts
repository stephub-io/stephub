import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import {
  NotificationService,
  ROUTE_ANIMATIONS_ELEMENTS,
} from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import {
  StepsCollection,
  Variable,
  Workspace,
} from "../workspace/workspace.model";
import { BehaviorSubject } from "rxjs";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";
import { MatChipInputEvent } from "@angular/material/chips";
import { FormControl, Validators } from "@angular/forms";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { VariableDialogComponent } from "../variable-dialog/variable-dialog.component";
import * as deepEqual from "fast-deep-equal";
import { initFeatures } from "./features/workspace-features.component";
import {
  FieldError,
  ServerError,
} from "../../../core/server-error/server-error.model";
import { inOutAnimation } from "../../../core/animations/in-out.animations";

@Component({
  selector: "sh-workspace-detail",
  templateUrl: "./workspace-detail.component.html",
  styleUrls: ["./workspace-detail.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [inOutAnimation],
})
export class WorkspaceDetailComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  id: string;
  workspace$: BehaviorSubject<Workspace> = new BehaviorSubject<Workspace>(null);
  workspace: Workspace;
  origWorkspace: Workspace;
  editable = true;
  editMode = false;
  variableKeys$ = new BehaviorSubject<string[]>([]);

  faExecutions = faExecutions;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  stepKeywordFormControl = new FormControl("", []);
  assignmentKeywordFormControl = new FormControl("", [
    Validators.pattern(/@ATTRIBUTE/),
  ]);
  validatorRequired = Validators.required;
  stepsCollection$ = new BehaviorSubject<StepsCollection>(null);

  fieldErrors$ = new BehaviorSubject<FieldError[]>(null);

  constructor(
    private workspaceService: WorkspaceService,
    private route: ActivatedRoute,
    private titleService: Title,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {
    this.route.params.subscribe((params) => (this.id = params.wid));
  }

  ngOnInit() {
    this.workspace$.pipe();
    this.workspaceService.get(this.id).subscribe((workspace) => {
      this.workspace$.next(workspace);
      this.onWorkspaceInit(workspace);
      return workspace;
    });
  }

  private onWorkspaceInit(workspace: Workspace) {
    initFeatures(workspace);
    this.workspace = workspace;
    this.setTitle(workspace);
    this.variableKeys$.next(this.keys(workspace.variables));
    this.origWorkspace = JSON.parse(JSON.stringify(workspace));
    this.workspaceService.getStepsCollection(this.id).subscribe(
      (collection) => {
        this.stepsCollection$.next(collection);
      },
      (error) => console.error("Failed to load steps collection", error)
    );
    this.fieldErrors$.next([]);
  }

  private setTitle(workspace: Workspace) {
    this.titleService.setTitle(`${workspace.name} - ${env.appName}`);
  }

  isDirty(): boolean {
    return !deepEqual(this.origWorkspace, this.workspace);
  }

  private onSyntaxChange() {
    this.workspace.gherkinPreferences = {
      ...this.workspace.gherkinPreferences,
    };
    this.stepsCollection$.next({
      ...this.stepsCollection$.value,
    });
  }

  addStepKeyword(
    keywords: string[],
    event: MatChipInputEvent,
    formControl: FormControl
  ) {
    const input = event.input;
    const value = event.value;
    if (formControl && !formControl.valid) {
      return;
    }
    if ((value || "").trim()) {
      keywords[keywords.length] = value.trim();
      this.onSyntaxChange();
    }

    // Reset the input value
    if (input) {
      input.value = "";
    }
  }

  removeStepKeyword(keywords: string[], keyword: string) {
    const index = keywords.indexOf(keyword);
    if (index >= 0) {
      keywords.splice(index, 1);
      this.onSyntaxChange();
    }
  }

  saveWorkspaceChanges() {
    this.editable = false;
    this.workspaceService.update(this.id, this.workspace).subscribe(
      (workspace) => {
        this.editable = true;
        this.workspace$.next(workspace);
        this.onWorkspaceInit(workspace);
        this.notificationService.success("Workspace saved successfully!");
      },
      (error) => {
        this.editable = true;
        if (error instanceof ServerError) {
          this.fieldErrors$.next(error.errors);
        }
        throw error;
      }
    );
  }

  discardWorkspaceChanges() {
    this.workspace$.next(this.origWorkspace);
    this.onWorkspaceInit(this.origWorkspace);
  }

  keys(obj: Object): string[] {
    return Object.keys(obj);
  }

  editVariable(name: string, variable: Variable) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      name: name,
      variable: variable,
      fieldErrors: this.fieldErrors$.value,
    };
    dialogConfig.width = "50%";

    this.dialog
      .open(VariableDialogComponent, dialogConfig)
      .afterClosed()
      .subscribe((data) => {
        if (data) {
          if (data.name != name) {
            delete this.workspace.variables[name];
          }
          this.workspace.variables[data.name] = data.variable;
          this.variableKeys$.next(this.keys(this.workspace.variables));
        }
      });
  }

  deleteVariable(key: string) {
    delete this.workspace.variables[key];
    this.variableKeys$.next(this.keys(this.workspace.variables));
  }

  addVariable() {
    this.editVariable("", {
      schema: { type: "string" },
      defaultValue: "",
    } as Variable);
  }

  variableColumns() {
    return this.editMode
      ? ["key", "schema", "defaultValue", "action"]
      : ["key", "schema", "defaultValue"];
  }

  allErrors() {
    if (this.workspace.errors) {
      return [...this.fieldErrors$.value, ...this.workspace.errors];
    } else {
      return this.fieldErrors$.value;
    }
  }
}
