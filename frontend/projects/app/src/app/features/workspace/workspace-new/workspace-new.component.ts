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
  selector: "sh-workspace-new",
  templateUrl: "./workspace-new.component.html",
  styleUrls: ["./workspace-new.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceNewComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  workspace$: BehaviorSubject<Workspace> = new BehaviorSubject<Workspace>(null);
  workspace: Workspace;

  fieldErrors$ = new BehaviorSubject<FieldError[]>(null);
  formGroup: FormGroup;

  constructor(
    private workspaceService: WorkspaceService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private _formBuilder: FormBuilder,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.formGroup = this._formBuilder.group({
      name: ["", Validators.required],
    });
    this.workspace$.pipe();
    this.workspaceService.getTemplate().subscribe((workspace) => {
      this.workspace$.next(workspace);
      this.workspace = workspace;
      return workspace;
    });
  }

  create() {
    this.fieldErrors$.next(null);
    this.workspaceService.create(this.workspace).subscribe(
      (workspace) => {
        this.notificationService.success("Workspace created successfully");
        this.router.navigate(["../", workspace.id], { relativeTo: this.route });
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
