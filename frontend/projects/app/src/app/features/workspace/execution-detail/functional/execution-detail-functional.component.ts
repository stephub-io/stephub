import { ChangeDetectionStrategy, Component } from "@angular/core";
import {
  ExecutionItem,
  FeatureExecutionItem,
  FixtureExecutionItem,
  FixtureType,
  FunctionalExecution,
  ScenarioExecutionItem,
  StepExecutionItem,
} from "../../execution.model";
import {
  faMagic,
  faReceipt,
  faRocket,
} from "@fortawesome/free-solid-svg-icons";
import { DatePipe } from "@angular/common";
import { ExecutionDetailBaseComponent } from "../execution-detail-base.component";
import { ExecutionService } from "../../execution.service";
import { ActivatedRoute } from "@angular/router";
import { BreadcrumbService } from "xng-breadcrumb";

@Component({
  selector: "sh-execution-detail-functional",
  templateUrl: "./execution-detail-functional.component.html",
  styleUrls: ["./execution-detail-functional.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
})
export class ExecutionDetailFunctionalComponent extends ExecutionDetailBaseComponent<
  FunctionalExecution
> {
  constructor(
    protected executionService: ExecutionService,
    protected route: ActivatedRoute,
    protected breadcrumbService: BreadcrumbService,
    protected datePipe: DatePipe
  ) {
    super(executionService, route, breadcrumbService, datePipe);
  }

  getFeature(item: ExecutionItem): FeatureExecutionItem {
    if (item.type == "feature") {
      return item as FeatureExecutionItem;
    }
    return null;
  }

  getName(item: ExecutionItem): string {
    switch (item.type) {
      case "feature":
        return (item as FeatureExecutionItem).name;
      case "scenario":
        return (item as ScenarioExecutionItem).name;
    }
    return null;
  }

  getIcon(item: ExecutionItem) {
    switch (item.type) {
      case "feature":
        return faRocket;
      case "scenario":
        return faReceipt;
      case "step":
        return faMagic;
    }
    return null;
  }

  getTitle(item: ExecutionItem) {
    switch (item.type) {
      case "feature":
        return "Feature";
      case "scenario":
        return "Scenario";
      case "step":
        return "Step";
    }
  }

  getStepItem(item: ExecutionItem) {
    if (item.type == "step") {
      return item as StepExecutionItem;
    }
    return null;
  }

  getFixtures(fixtures: FixtureExecutionItem[], type: FixtureType) {
    return fixtures
      .filter((f) => f.type == type)
      .sort((a, b) => a.priority - b.priority);
  }

  trackByIndex(index: number): number {
    return index;
  }
}
