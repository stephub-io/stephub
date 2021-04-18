import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
} from "@angular/core";
import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../../core/core.module";
import { LoadStats } from "../../../execution.model";

@Component({
  selector: "sh-load-stats-bar",
  templateUrl: "./load-stats-bar.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadStatsBarComponent implements OnChanges {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() stats: LoadStats;
  rows: StatsRow[];
  overallTooltip: string;

  ngOnChanges() {
    const rows = (this.rows = []);
    const stats = this.stats;
    const total =
      stats.cancelled + stats.failed + stats.erroneous + stats.passed;

    function buildStatsRow(
      name: string,
      amount: number,
      cssClass: string,
      mandatory: boolean
    ) {
      if (mandatory || amount > 0) {
        rows.push({
          name,
          amount,
          ratio: total ? Math.round((amount / total) * 100) : 0,
          cssClass,
          total,
        } as StatsRow);
      }
    }

    buildStatsRow("passed", stats.passed, "bg-success", true);
    buildStatsRow("failed", stats.failed, "bg-danger", false);
    buildStatsRow("erroneous", stats.erroneous, "bg-danger", false);
    buildStatsRow("cancelled", stats.cancelled, "bg-secondary", false);

    this.overallTooltip = "";
    this.rows.forEach(
      (row) =>
        (this.overallTooltip +=
          row.name +
          ": " +
          row.ratio +
          "% - " +
          row.amount +
          "/" +
          row.total +
          "\n")
    );
    this.overallTooltip = this.overallTooltip.trim();
  }
}

export interface StatsRow {
  ratio: number;
  name: string;
  amount: number;
  cssClass: string;
  total: number;
}
