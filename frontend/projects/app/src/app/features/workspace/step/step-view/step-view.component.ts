import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import { parse, StepInstruction } from "../parser/instruction-parser";
import { GherkinPreferences } from "../../workspace/workspace.model";

@Component({
  selector: "sh-step-view",
  templateUrl: "./step-view.component.html",
  styleUrls: ["./step-view.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepViewComponent implements OnInit {
  @Input() step: string;

  @Input() gherkinPreferences: GherkinPreferences;

  instruction: StepInstruction;

  constructor() {}

  ngOnInit(): void {
    this.instruction = parse(this.step, null, false, this.gherkinPreferences);
  }
}
