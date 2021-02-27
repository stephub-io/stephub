import { ColumnSpec, PayloadType, StepSpec } from "../step.model";
import {
  DataTable,
  DocString,
  Fragment,
  StepInstruction,
  StepLine,
  StepLinePart,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "../parser/instruction-parser";
import { GherkinPreferences } from "../../workspace/workspace.model";
import { getSchemaExample } from "../../../../shared/json-schema-view/json--schema-view.component";

export class SpecSuggest {
  private textParts: string[];
  public spec: StepSpec;
  private payloadFragments: Fragment[] = [];

  constructor(spec: StepSpec) {
    this.spec = spec;
    this.textParts = spec.pattern.split(
      /(?<=^|[^\\])\{(?:[a-zA-Z_][a-zA-Z0-9_]*)\}/
    );
    if (spec.payload == PayloadType.DOC_STRING) {
      this.payloadFragments.push(
        new DocString(
          spec.docString?.doc?.examples?.length > 0
            ? spec.docString.doc.examples[0].value.split("\n")
            : [""]
        )
      );
    } else if (spec.payload == PayloadType.DATA_TABLE) {
      const rows: string[][] = [];
      if (spec.dataTable.header) {
        rows[0] = [];
        spec.dataTable.columns.forEach((colSpec) => rows[0].push(colSpec.name));
      }
      const i = rows.length;
      rows[i] = [];
      spec.dataTable.columns.forEach((colSpec) =>
        rows[i].push(this.getExample(colSpec))
      );
      const tableRows: string[] = [];
      rows.forEach((row) => tableRows.push("| " + row.join(" | ") + " |"));
      this.payloadFragments.push(new DataTable(tableRows));
    }
  }

  private getExample(colSpec: ColumnSpec): string {
    return (
      colSpec.doc?.examples[0]?.value ||
      JSON.stringify(getSchemaExample(colSpec.schema))
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

  public buildSuggestion(
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
          return new StepInstruction(
            this.addPayloadFragments([
              new StepLine([
                ...stepLineParts,
                new StepLinePartAssignment(
                  " " + aParts[0],
                  "${attribute}",
                  aParts[1]
                ),
              ]),
            ])
          );
        }),
        new StepInstruction(
          this.addPayloadFragments([new StepLine(stepLineParts)])
        ),
      ];
    } else {
      return [
        new StepInstruction(
          this.addPayloadFragments([new StepLine(stepLineParts)])
        ),
      ];
    }
  }

  private addPayloadFragments(fragments: Fragment[]): Fragment[] {
    if (this.payloadFragments.length > 0) {
      return [...fragments, ...this.payloadFragments];
    }
    return fragments;
  }
}
