import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/core.module";
import { WorkspaceService } from "../workspace/workspace.service";
import { ExecutionService } from "../execution.service";
import {
  ExecutionsResult,
  ExecutionStatus,
  ExecutionType,
  FunctionalExecution,
} from "../execution.model";
import { Observable } from "rxjs";
import { ActivatedRoute } from "@angular/router";
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
  executions$: Observable<ExecutionsResult<FunctionalExecution>>;

  constructor(
    private workspaceService: WorkspaceService,
    private executionService: ExecutionService,
    private route: ActivatedRoute
  ) {
    this.route.parent.parent.params.subscribe((params) => {
      this.wid = params.wid;
    });
  }

  ngOnInit() {
    this.executions$ = this.executionService.fetch(
      this.wid,
      ExecutionType.FUNCTIONAL
    );
  }

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }
}
