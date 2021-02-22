import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import {
  DataTable,
  DocString,
  StepInstruction,
  StepLine,
  StepLinePart,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "../parser/instruction-parser";
import { PayloadType } from "../step.model";

@Component({
  selector: "sh-step-abstract",
  templateUrl: "./step-abstract.component.html",
  styleUrls: ["./step-abstract.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepAbstractComponent implements OnInit {
  @Input() instruction: StepInstruction;
  @Input() indicatePayload = true;
  stepLine: StepLine;
  payload: PayloadType = null;

  constructor() {}

  ngOnInit(): void {
    if (
      this.instruction.fragments.filter((value) => value instanceof DocString)
        .length > 0
    ) {
      this.payload = PayloadType.DOC_STRING;
    }
    if (
      this.instruction.fragments.filter((value) => value instanceof DataTable)
        .length > 0
    ) {
      this.payload = PayloadType.DATA_TABLE;
    }
    let sl = this.instruction.fragments.filter(
      (value) => value instanceof StepLine
    );
    this.stepLine = sl.length > 0 ? (sl[0] as StepLine) : null;
  }

  keyword(part: StepLinePart): StepLinePartKeyword {
    if (part instanceof StepLinePartKeyword) {
      return part as StepLinePartKeyword;
    }
    return null;
  }

  text(part: StepLinePart): StepLinePartText {
    if (part instanceof StepLinePartText) {
      return part as StepLinePartText;
    }
    return null;
  }

  assigment(part: StepLinePart): StepLinePartAssignment {
    if (part instanceof StepLinePartAssignment) {
      return part as StepLinePartAssignment;
    }
    return null;
  }

  attribute(part: StepLinePart): StepLinePartAttribute {
    if (part instanceof StepLinePartAttribute) {
      return part as StepLinePartAttribute;
    }
    return null;
  }
}
