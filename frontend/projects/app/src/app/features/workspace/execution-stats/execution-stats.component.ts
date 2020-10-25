import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { Stats } from "../execution.model";

@Component({
  selector: "sh-execution-stats",
  templateUrl: "./execution-stats.component.html",
  styleUrls: ["./execution-stats.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionStatsComponent {
  @Input() stats: Stats;

  constructor() {}

  showCount(): boolean {
    return this.stats.erroneous + this.stats.failed + this.stats.passed != 1;
  }
}
