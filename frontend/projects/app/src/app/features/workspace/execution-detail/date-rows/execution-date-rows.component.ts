import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../core/core.module";

const humanizeDuration = require("humanize-duration");

@Component({
  selector: "sh-execution-date-rows",
  templateUrl: "./execution-date-rows.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionDateRowsComponent {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() initiatedAt: string;
  @Input() startedAt: string;
  @Input() completedAt: string;
  @Input() now: Date;

  duration() {
    if (this.startedAt) {
      if (this.completedAt) {
        return this.durationBetween(
          new Date(this.startedAt),
          new Date(this.completedAt)
        );
      } else {
        return (
          "for " +
          this.durationBetween(
            new Date(this.startedAt),
            this.now ? this.now : new Date()
          )
        );
      }
    }
    return "-";
  }

  private durationBetween(from: Date, to: Date) {
    const duration = to.getTime() - from.getTime();
    return humanizeDuration(duration);
  }
}
