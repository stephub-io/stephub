import { StepSpec } from "../step.model";
import {
  StepInstruction,
  StepLine,
  StepLinePart,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "../parser/instruction-parser";
import { GherkinPreferences } from "../../workspace/workspace.model";

export class SpecSuggest {
  private textParts: string[];
  private spec: StepSpec;

  constructor(spec: StepSpec) {
    this.spec = spec;
    this.textParts = spec.pattern.split(
      /(?<=^|[^\\])\{(?:[a-zA-Z_][a-zA-Z0-9_]*)\}/
    );
  }

  completes(
    keyword: string,
    stepInput: string,
    prefs: GherkinPreferences
  ): StepInstruction[] {
    const filterValue = stepInput.trim().toLowerCase();
    if (
      this.textParts.findIndex((option) =>
        option.toLowerCase().includes(filterValue)
      ) >= 0
    ) {
      return this.buildSuggestion(keyword, prefs);
    }
    return [];
  }

  private buildSuggestion(
    keyword: string,
    prefs: GherkinPreferences
  ): StepInstruction[] {
    let stepLineParts: StepLinePart[] = [new StepLinePartKeyword(keyword)];
    let argsReg = /((?<=^|[^\\])\{(?:[a-zA-Z_][a-zA-Z0-9_]*)\})/g;
    this.textParts.forEach((value, index) => {
      if (index > 0) {
        let args = argsReg.exec(this.spec.pattern);
        if (args) {
          stepLineParts.push(new StepLinePartAttribute(args[1]));
        }
      }
      stepLineParts.push(new StepLinePartText((index == 0 ? " " : "") + value));
    });
    if (this.spec.output && prefs?.assignmentKeywords) {
      return [
        ...prefs.assignmentKeywords.map((assignment) => {
          const aParts = assignment.split(/@attribute/i);
          return new StepInstruction([
            new StepLine([
              ...stepLineParts,
              new StepLinePartAssignment(
                " " + aParts[0],
                "${attribute}",
                aParts[1]
              ),
            ]),
          ]);
        }),
        new StepInstruction([new StepLine(stepLineParts)]),
      ];
    } else {
      return [new StepInstruction([new StepLine(stepLineParts)])];
    }
  }
}
