import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import {
  StepInstruction,
  StepLine,
  StepLinePart,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "../parser/instruction-parser";

@Component({
  selector: "sh-step-abstract",
  templateUrl: "./step-abstract.component.html",
  styleUrls: ["./step-abstract.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepAbstractComponent {
  @Input() instruction: StepInstruction;

  constructor() {}

  stepLine(): StepLine {
    let sl = this.instruction.fragments.filter(
      (value) => value instanceof StepLine
    );
    return sl.length > 0 ? (sl[0] as StepLine) : null;
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
