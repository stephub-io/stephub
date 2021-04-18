import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../core/core.module";
import {
  Execution,
  ExecutionStatus,
  FixtureExecutionItem,
  FixtureType,
} from "../../execution.model";
import { statusIcon, statusIconSpin } from "../execution-detail-base.component";

@Component({
  selector: "sh-execution-detail-fixtures",
  templateUrl: "./execution-detail-fixtures.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionDetailFixturesComponent implements OnChanges {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() showType: boolean = false;

  @Input() wid: string;
  @Input() exec: Execution;
  @Input() fixtures: FixtureExecutionItem[];
  orderedFixtures: FixtureExecutionItem[];

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }

  private ordinal(type: FixtureType) {
    switch (type) {
      case FixtureType.before:
        return 1;
      case FixtureType.after:
        return 2;
      default:
        return 0;
    }
  }

  trackByFixture(index: number): number {
    return index;
  }

  ngOnChanges(): void {
    this.orderedFixtures = this.fixtures.sort(
      (a, b) =>
        (this.ordinal(a.type) - this.ordinal(b.type)) * 1000 +
        (a.priority - b.priority)
    );
  }
}
