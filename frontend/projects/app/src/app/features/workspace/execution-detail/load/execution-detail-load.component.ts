import { ChangeDetectionStrategy, Component } from "@angular/core";
import { DatePipe } from "@angular/common";
import { ExecutionDetailBaseComponent } from "../execution-detail-base.component";
import {
  ExecutionStatus,
  LoadExecution,
  LoadScenarioRun,
  LoadSimulation,
  LoadStep,
  RunnerStatus,
  Stats,
} from "../../execution.model";
import { ExecutionService } from "../../execution.service";
import { ActivatedRoute } from "@angular/router";
import { BreadcrumbService } from "xng-breadcrumb";
import { GherkinPreferences } from "../../workspace/workspace.model";
import { parse } from "../../step/parser/instruction-parser";
import { faMagic } from "@fortawesome/free-solid-svg-icons";
import { BehaviorSubject } from "rxjs";
import { StepStatus } from "../../step.model";

@Component({
  selector: "sh-execution-detail-load",
  templateUrl: "./execution-detail-load.component.html",
  styleUrls: ["./execution-detail-load.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
})
export class ExecutionDetailLoadComponent extends ExecutionDetailBaseComponent<
  LoadExecution
> {
  stepIcon = faMagic;
  stepsStatsColumns = ["name", "min", "avg", "max", "status"];

  constructor(
    protected executionService: ExecutionService,
    protected route: ActivatedRoute,
    protected breadcrumbService: BreadcrumbService,
    protected datePipe: DatePipe
  ) {
    super(executionService, route, breadcrumbService, datePipe);
  }

  ngOnInit() {
    super.ngOnInit();
  }

  mapRunnerStatus(status: RunnerStatus): ExecutionStatus {
    switch (status) {
      case RunnerStatus.initiated:
        return ExecutionStatus.initiated;
      case RunnerStatus.running:
        return ExecutionStatus.executing;
      case RunnerStatus.stopping:
        return ExecutionStatus.stopping;
      case RunnerStatus.stopped:
        return ExecutionStatus.completed;
      default:
        return ExecutionStatus.cancelled;
    }
  }

  trackByIndex(index: number): number {
    return index;
  }

  parseStepInstruction(step: LoadStep, preferences: GherkinPreferences) {
    return parse(step.step, step.spec, true, preferences);
  }
}
