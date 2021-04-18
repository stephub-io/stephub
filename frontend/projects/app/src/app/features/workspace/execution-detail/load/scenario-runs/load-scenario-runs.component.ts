import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../../core/core.module";
import {
  LoadExecution,
  LoadScenario,
  LoadScenarioRun,
  LoadSimulation,
  PageResult,
  Stats,
} from "../../../execution.model";
import { BehaviorSubject } from "rxjs";
import { ExecutionService } from "../../../execution.service";
import { StepStatus } from "../../../step.model";

@Component({
  selector: "sh-load-scenario-runs",
  templateUrl: "./load-scenario-runs.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadScenarioRunsComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() wid: string;
  @Input() execution: LoadExecution;
  @Input() simulation: LoadSimulation;
  @Input() now: Date;

  loading: boolean;
  failedScenarios$ = new BehaviorSubject<PageResult<LoadScenarioRun>>({
    total: 0,
    items: [],
  });

  constructor(protected executionService: ExecutionService) {}

  ngOnInit(): void {
    this.loadMore();
  }

  loadMore() {
    const current = this.failedScenarios$.value;
    this.loading = true;
    this.executionService
      .fetchLoadRuns(
        this.wid,
        this.execution.id,
        this.simulation.id,
        current.items.length,
        25
      )
      .subscribe(
        (result) => {
          this.failedScenarios$.next({
            total: result.total,
            items: [...current.items, ...result.items],
          });
          this.loading = false;
        },
        () => (this.loading = false)
      );
  }

  trackByIndex(index: number): number {
    return index;
  }

  scenario(fs: LoadScenarioRun, simulation: LoadSimulation): LoadScenario {
    return simulation.scenarios.filter((s) => s.id == fs.scenarioId)[0];
  }

  scenarioStats(fs: LoadScenarioRun): Stats {
    return {
      passed: fs.status == StepStatus.passed ? 1 : 0,
      erroneous: fs.status == StepStatus.erroneous ? 1 : 0,
      failed: fs.status == StepStatus.failed ? 1 : 0,
    } as Stats;
  }
}
