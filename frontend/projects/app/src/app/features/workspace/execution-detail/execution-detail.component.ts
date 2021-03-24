import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/core.module";
import { ExecutionService } from "../execution.service";
import {
  Execution,
  ExecutionItem,
  ExecutionLogAttachment,
  ExecutionStatus,
  FeatureExecutionItem,
  FunctionalExecution,
  ScenarioExecutionItem,
  StepExecutionItem,
} from "../execution.model";
import { Observable } from "rxjs";
import {
  faMagic,
  faReceipt,
  faRocket,
  faSpinner,
} from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import {
  faCheckCircle,
  faFileAlt,
  faFileImage,
  faPauseCircle,
  faTimesCircle,
} from "@fortawesome/free-regular-svg-icons";
import { parse } from "../step/parser/instruction-parser";
import { GherkinPreferences } from "../workspace/workspace.model";
import { map } from "rxjs/operators";
import { BreadcrumbService } from "xng-breadcrumb";
import { DatePipe } from "@angular/common";
import { StepStatus } from "../step.model";

@Component({
  selector: "sh-execution-detail",
  templateUrl: "./execution-detail.component.html",
  styleUrls: ["./execution-detail.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
})
export class ExecutionDetailComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  wid: string;
  id: string;
  execution$: Observable<FunctionalExecution>;

  constructor(
    private executionService: ExecutionService,
    private route: ActivatedRoute,
    private breadcrumbService: BreadcrumbService,
    private datePipe: DatePipe
  ) {
    this.route.parent.parent.params.subscribe((params) => {
      this.wid = params.wid;
    });
    this.route.params.subscribe((params) => {
      this.id = params.id;
    });
  }

  ngOnInit() {
    this.execution$ = this.executionService
      .get<FunctionalExecution>(this.wid, this.id)
      .pipe(
        map((execution) => {
          this.breadcrumbService.set(
            "@execution",
            execution.startedAt
              ? this.datePipe.transform(execution.startedAt, "medium")
              : "Starting soon"
          );
          return execution;
        })
      );
  }

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
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

  getStepItems(item: any): StepExecutionItem[] {
    return item as StepExecutionItem[];
  }

  parseStepInstruction(
    stepItem: StepExecutionItem,
    preferences: GherkinPreferences
  ) {
    return parse(stepItem.step, stepItem.stepSpec, true, preferences);
  }

  getStepItem(item: ExecutionItem) {
    if (item.type == "step") {
      return item as StepExecutionItem;
    }
    return null;
  }

  errorStep(step: StepExecutionItem) {
    return (
      step.erroneous ||
      step.response?.status == StepStatus.erroneous ||
      step.response?.status == StepStatus.failed
    );
  }

  aggAttachmentIcon(step: StepExecutionItem) {
    const attachments: ExecutionLogAttachment[] = [];
    step.response?.logs?.forEach((log) =>
      log?.attachments.forEach((a) => attachments.push(a))
    );
    if (attachments.findIndex((a) => a.contentType.startsWith("image")) >= 0) {
      return faFileImage;
    } else if (attachments.length > 0) {
      return faFileAlt;
    }
    return null;
  }

  imageAttachments(attachments: ExecutionLogAttachment[]) {
    return attachments
      .filter((a) => a.contentType.startsWith("image"))
      .map((a) =>
        this.executionService.getAttachmentHref(this.wid, this.id, a)
      );
  }

  attachmentUrl(a: ExecutionLogAttachment) {
    return this.executionService.getAttachmentHref(this.wid, this.id, a);
  }

  attachmentIcon(a: ExecutionLogAttachment) {
    if (a.contentType.startsWith("image")) {
      return faFileImage;
    }
    return faFileAlt;
  }
}

export function statusIcon(status: ExecutionStatus) {
  switch (status) {
    case ExecutionStatus.cancelled:
      return faTimesCircle;
    case ExecutionStatus.completed:
      return faCheckCircle;
    case ExecutionStatus.initiated:
      return faPauseCircle;
    case ExecutionStatus.executing:
      return faSpinner;
  }
}

export function statusIconSpin(status: ExecutionStatus) {
  return status == ExecutionStatus.executing;
}
