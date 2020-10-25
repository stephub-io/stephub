import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { Workspace } from "../workspace/workspace.model";
import { ExecutionService } from "../execution.service";
import { ExecutionsResult, ExecutionStatus } from "../execution.model";
import { Observable } from "rxjs";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import {
  faCheckCircle,
  faPauseCircle,
  faTimesCircle,
} from "@fortawesome/free-regular-svg-icons";
import {
  statusIcon,
  statusIconSpin,
} from "../execution-detail/execution-detail.component";

@Component({
  selector: "sh-execution-list",
  templateUrl: "./execution-list.component.html",
  styleUrls: ["./execution-list.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionListComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  wid: string;
  executions$: Observable<ExecutionsResult>;

  constructor(
    private workspaceService: WorkspaceService,
    private executionService: ExecutionService,
    private route: ActivatedRoute
  ) {
    this.route.parent.params.subscribe((params) => {
      this.wid = params.wid;
    });
  }

  ngOnInit() {
    this.executions$ = this.executionService.fetch(this.wid);
  }

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }
}
