import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import {
  CommentLine,
  DataTable,
  DocString,
  Fragment,
  StepInstruction,
  StepLine,
} from "../parser/instruction-parser";

@Component({
  selector: "sh-step-request",
  templateUrl: "./step-request.component.html",
  styleUrls: ["./step-request.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepRequestComponent {
  @Input() instruction: StepInstruction;

  constructor() {}

  stepLine(fragment: Fragment): StepLine {
    if (fragment instanceof StepLine) {
      return fragment as StepLine;
    }
    return null;
  }

  commentLine(fragment: Fragment): CommentLine {
    if (fragment instanceof CommentLine) {
      return fragment as CommentLine;
    }
    return null;
  }

  docString(fragment: Fragment): DocString {
    if (fragment instanceof DocString) {
      return fragment as DocString;
    }
    return null;
  }

  dataTable(fragment: Fragment): DataTable {
    if (fragment instanceof DataTable) {
      return fragment as DataTable;
    }
    return null;
  }
}
