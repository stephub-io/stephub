import {
  ChangeDetectionStrategy,
  Component,
  Input,
  QueryList,
  ViewChildren,
} from "@angular/core";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import { StepSequence, Workspace } from "../../workspace/workspace.model";
import {
  faMagic,
  faReceipt,
  faRocket,
} from "@fortawesome/free-solid-svg-icons";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { MatInput } from "@angular/material/input";
import { Validators } from "@angular/forms";

@Component({
  selector: "sh-workspace-features",
  templateUrl: "./workspace-features.component.html",
  styleUrls: ["./workspace-features.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceFeaturesComponent {
  featureIcon = faRocket;
  scenarioIcon = faReceipt;
  stepIcon = faMagic;

  @Input() workspace: Workspace;

  @ViewChildren("stepEdit") stepEditFields: QueryList<MatInput>;

  validatorRequired = Validators.required;

  constructor() {}

  ngOnInit() {
    if (!this.workspace.features) {
      this.workspace.features = [];
    }
    this.workspace.features.forEach((feature) => {
      if (!feature.background) {
        feature.background = {
          steps: [],
        };
      }
      if (!feature.scenarios) {
        feature.scenarios = [];
      }
      feature.scenarios.forEach((scenario) => {
        if (!scenario.steps) {
          scenario.steps = [];
        }
      });
    });
  }

  onStepDrop(sequence: StepSequence, event: CdkDragDrop<string[]>) {
    moveItemInArray(sequence.steps, event.previousIndex, event.currentIndex);
    sequence.steps = [...sequence.steps];
  }

  addStep(sequence: StepSequence, newStep: HTMLInputElement) {
    if (!sequence.steps) {
      sequence.steps = [];
    }
    sequence.steps.push(newStep.value);
    sequence.steps = [...sequence.steps];
    newStep.value = "";
  }

  deleteStep(sequence: StepSequence, index: number) {
    sequence.steps.splice(index, 1);
    sequence.steps = [...sequence.steps];
  }

  addStepOnEnter(
    event: KeyboardEvent,
    sequence: StepSequence,
    newStep: HTMLInputElement
  ) {
    if (!event.shiftKey && newStep.value.indexOf("\n") < 0) {
      this.addStep(sequence, newStep);
      event.preventDefault();
    }
  }

  saveStep(sequence: StepSequence, index: number, newValue: string) {
    sequence.steps[index] = newValue;
    sequence.steps = [...sequence.steps];
  }
}
