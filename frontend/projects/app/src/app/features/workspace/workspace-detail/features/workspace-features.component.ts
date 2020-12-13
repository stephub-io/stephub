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

  constructor() {}

  ngOnInit() {}

  onStepDrop(sequence: StepSequence, event: CdkDragDrop<string[]>) {
    moveItemInArray(sequence.steps, event.previousIndex, event.currentIndex);
    sequence.steps = [...sequence.steps];
  }

  addStep(sequence: StepSequence, newStep: HTMLInputElement) {
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

  saveStep(
    sequence: StepSequence,
    editRows: boolean[],
    editRowsData: string[],
    index: number
  ) {
    sequence.steps[index] = editRowsData[index];
    sequence.steps = [...sequence.steps];
    editRows[index] = false;
  }

  saveStepOnEnter(
    event: KeyboardEvent,
    sequence: StepSequence,
    editRows: boolean[],
    editRowsData: string[],
    index: number
  ) {
    if (!event.shiftKey && editRowsData[index].indexOf("\n") < 0) {
      this.saveStep(sequence, editRows, editRowsData, index);
      event.preventDefault();
    }
  }

  editStep(
    sequence: StepSequence,
    editRows: boolean[],
    editRowsData: string[],
    index: number
  ) {
    editRowsData[index] = sequence.steps[index];
    editRows[index] = true;
  }
}
