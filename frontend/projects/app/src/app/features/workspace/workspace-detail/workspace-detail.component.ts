import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import {
  NotificationService,
  ROUTE_ANIMATIONS_ELEMENTS,
} from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { Workspace } from "../workspace/workspace.model";
import { BehaviorSubject } from "rxjs";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";
import { MatChipInputEvent } from "@angular/material/chips";
import { FormControl, Validators } from "@angular/forms";

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

  faExecutions = faExecutions;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  stepKeywordFormControl = new FormControl("", []);
  assignmentKeywordFormControl = new FormControl("", [
    Validators.pattern(/@ATTRIBUTE/),
  ]);

  constructor(
    private workspaceService: WorkspaceService,
    private route: ActivatedRoute,
    private titleService: Title,
    private notificationService: NotificationService
  ) {
    this.route.params.subscribe((params) => (this.id = params.wid));
  }

  ngOnInit() {
    this.workspaceService.get(this.id).subscribe((workspace) => {
      this.workspace = workspace;
      this.workspace$.next(workspace);
      this.setTitle(workspace);
      return workspace;
    });
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
      this.update();
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
      this.update();
    }
  }

  private update() {
    this.workspaceService
      .patch(this.id, this.workspace)
      .subscribe((workspace) => {
        this.workspace = workspace;
        this.workspace$.next(workspace);
        this.setTitle(workspace);
        this.notificationService.success("Workspace changes were saved");
      });
  }
}
