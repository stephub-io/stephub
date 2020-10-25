import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { Workspace, WorkspacesResult } from "../workspace/workspace.model";
import { Observable } from "rxjs";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: "sh-workspace-list",
  templateUrl: "./workspace-list.component.html",
  styleUrls: ["./workspace-list.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceListComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  workspaces$: Observable<WorkspacesResult>;

  faExecutions = faExecutions;

  constructor(private workspaceService: WorkspaceService) {}

  ngOnInit() {
    this.workspaces$ = this.workspaceService.fetch();
  }
}
