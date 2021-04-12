import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../core/core.module";
import {
  Execution,
  ExecutionStatus,
  FixtureExecutionItem,
} from "../../execution.model";
import { statusIcon, statusIconSpin } from "../execution-detail-base.component";

@Component({
  selector: "sh-execution-detail-fixtures",
  templateUrl: "./execution-detail-fixtures.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionDetailFixturesComponent {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() wid: string;
  @Input() exec: Execution;
  @Input() fixtures: FixtureExecutionItem[];

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }
}
