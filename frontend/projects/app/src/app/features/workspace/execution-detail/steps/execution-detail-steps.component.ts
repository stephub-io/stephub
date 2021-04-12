import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";

import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../../core/core.module";
import { ExecutionService } from "../../execution.service";
import {
  Execution,
  ExecutionLogAttachment,
  ExecutionStatus,
  StepExecutionItem,
} from "../../execution.model";
import { faMagic } from "@fortawesome/free-solid-svg-icons";
import { faFileAlt, faFileImage } from "@fortawesome/free-regular-svg-icons";
import { parse } from "../../step/parser/instruction-parser";
import { GherkinPreferences } from "../../workspace/workspace.model";
import { StepStatus } from "../../step.model";
import { statusIcon, statusIconSpin } from "../execution-detail-base.component";

@Component({
  selector: "sh-execution-detail-steps",
  templateUrl: "./execution-detail-steps.component.html",
  styleUrls: ["./execution-detail-steps.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionDetailStepsComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  @Input() wid: string;
  @Input() exec: Execution;
  @Input() stepItems: StepExecutionItem[];

  stepIcon = faMagic;

  constructor(private executionService: ExecutionService) {}

  ngOnInit() {}

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }

  parseStepInstruction(
    stepItem: StepExecutionItem,
    preferences: GherkinPreferences
  ) {
    return parse(stepItem.step, stepItem.stepSpec, true, preferences);
  }

  errorStep(step: StepExecutionItem) {
    return (
      step.erroneous ||
      step.result?.status == StepStatus.erroneous ||
      step.result?.status == StepStatus.failed
    );
  }

  aggAttachmentIcon(step: StepExecutionItem) {
    const attachments: ExecutionLogAttachment[] = [];
    step.result?.logs?.forEach((log) =>
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
        this.executionService.getAttachmentHref(this.wid, this.exec.id, a)
      );
  }

  attachmentUrl(a: ExecutionLogAttachment) {
    return this.executionService.getAttachmentHref(this.wid, this.exec.id, a);
  }

  attachmentIcon(a: ExecutionLogAttachment) {
    if (a.contentType.startsWith("image")) {
      return faFileImage;
    }
    return faFileAlt;
  }
}
