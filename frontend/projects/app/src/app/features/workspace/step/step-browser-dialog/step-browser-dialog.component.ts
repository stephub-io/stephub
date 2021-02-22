import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
} from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import { SpecSuggest } from "../spec-suggest/spec-suggest";
import { StepSpec } from "../step.model";
import { StepInstruction } from "../parser/instruction-parser";
import { BehaviorSubject } from "rxjs";
import { StepsCollection } from "../../workspace/workspace.model";

@Component({
  selector: "sh-step-browser-dialog",
  templateUrl: "./step-browser-dialog.component.html",
  styleUrls: ["./step-browser-dialog.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepBrowserDialogComponent implements OnInit {
  stepCollections: CollectionWrapper[] = [];
  filteredSteps$ = new BehaviorSubject<CollectionWrapper[]>([]);
  filtered: boolean;
  expandedByDefault: boolean;

  constructor(
    private dialogRef: MatDialogRef<StepBrowserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) {
    Object.entries(data.stepsCollection as StepsCollection).forEach(
      ([name, collection]) => {
        this.stepCollections.push(
          new CollectionWrapper(
            name,
            collection
              .sort((one: StepSpec, two: StepSpec) =>
                one.pattern > two.pattern ? -1 : 1
              )
              .map(
                (spec: StepSpec) =>
                  new StepWrapper(
                    spec,
                    new SpecSuggest(spec).buildSuggestion("...", null)[0]
                  )
              )
          )
        );
      }
    );
    this.filteredSteps$.next(this.stepCollections);
    if (data.filterBySpec) {
      this.filtered = true;
      this.expandedByDefault = true;
      const filterBySpec = data.filterBySpec as StepSpec;
      this.filteredSteps$.next(
        this.stepCollections
          .map(
            (cw) =>
              new CollectionWrapper(
                cw.name,
                cw.steps.filter((sw) => sw.spec.id == filterBySpec.id)
              )
          )
          .filter((cw) => cw.steps.length > 0)
      );
    }
  }

  ngOnInit(): void {}

  close() {
    this.dialogRef.close();
  }
}

class StepWrapper {
  spec: StepSpec;
  instruction: StepInstruction;

  constructor(spec: StepSpec, instruction: StepInstruction) {
    this.spec = spec;
    this.instruction = instruction;
  }
}

class CollectionWrapper {
  name: string;
  steps: StepWrapper[];

  constructor(name: string, steps: StepWrapper[]) {
    this.name = name;
    this.steps = steps;
  }
}
