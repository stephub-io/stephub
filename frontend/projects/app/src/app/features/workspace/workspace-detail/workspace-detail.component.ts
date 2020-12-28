import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import {
  NotificationService,
  ROUTE_ANIMATIONS_ELEMENTS,
} from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { Variable, Workspace } from "../workspace/workspace.model";
import { BehaviorSubject, Observable } from "rxjs";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";
import { MatChipInputEvent } from "@angular/material/chips";
import { FormControl, Validators } from "@angular/forms";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { VariableDialogComponent } from "./variable-dialog/variable-dialog.component";

@Component({
  selector: "sh-workspace-detail",
  templateUrl: "./workspace-detail.component.html",
  styleUrls: ["./workspace-detail.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceDetailComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  id: string;
  workspace$: BehaviorSubject<Workspace> = new BehaviorSubject<Workspace>(null);
  workspace: Workspace;
  editable = true;
  variablesMap;

  faExecutions = faExecutions;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  stepKeywordFormControl = new FormControl("", []);
  assignmentKeywordFormControl = new FormControl("", [
    Validators.pattern(/@ATTRIBUTE/),
  ]);
  validatorRequired = Validators.required;

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
    this.workspaceService.get(this.id).subscribe((workspace) => {
      this.workspace$.next(workspace);
      this.onWorkspaceInit(workspace);
      return workspace;
    });
  }

  private onWorkspaceInit(workspace: Workspace) {
    this.workspace = workspace;
    this.setTitle(workspace);
    this.variablesMap = this.arrayOfMap(workspace.variables);
  }

  private setTitle(workspace: Workspace) {
    this.titleService.setTitle(`${workspace.name} - ${env.appName}`);
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
      keywords.push(value.trim());
      this.patchGherkinPreferences();
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
      this.patchGherkinPreferences();
    }
  }

  private patchGherkinPreferences() {
    return this.patch({
      gherkinPreferences: this.workspace.gherkinPreferences,
    } as Workspace).subscribe();
  }

  private patch(patch: Workspace): Observable<Workspace> {
    this.editable = false;
    return new Observable((observer) => {
      this.workspaceService.patch(this.id, patch).subscribe(
        (workspace) => {
          this.editable = true;
          this.workspace$.next(workspace);
          this.onWorkspaceInit(workspace);
          this.notificationService.success("Workspace changes were saved");
          observer.next(workspace);
        },
        (error) => {
          this.editable = true;
          observer.error(error);
        }
      );
    });
  }

  keys(obj: Object): string[] {
    return Object.keys(obj);
  }

  arrayOfMap(map: Object) {
    return this.keys(map).map((value, index) => {
      return {
        key: value,
        value: map[value],
      };
    });
  }

  editVariable(name: string, variable: Variable) {
    const dialogConfig = new MatDialogConfig();
    // dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    let dialogRef;

    dialogConfig.data = {
      name: name,
      variable: variable,
      saveCallback: (data) => {
        const newVars = Object.assign({}, this.workspace.variables);
        delete newVars[name];
        newVars[data.name] = data.variable;
        return this.patch({
          variables: newVars,
        } as Workspace);
      },
    };
    dialogConfig.width = "50%";

    dialogRef = this.dialog.open(VariableDialogComponent, dialogConfig);
  }

  deleteVariable(key: string) {
    const newVars = Object.assign({}, this.workspace.variables);
    delete newVars[key];
    return this.patch({
      variables: newVars,
    } as Workspace).subscribe();
  }

  addVariable() {
    this.editVariable("", {
      schema: { type: "string" },
    } as Variable);
  }
}
