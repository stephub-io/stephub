import {
  ChangeDetectionStrategy,
  Component,
  ContentChild,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from "@angular/core";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { FormControl, ValidatorFn } from "@angular/forms";
import { MultiStringViewDirective } from "./multi-string-view.directive";

@Component({
  selector: "sh-multi-string-input",
  templateUrl: "./multi-string-input.component.html",
  styleUrls: ["./multi-string-input.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiStringInputComponent implements OnInit {
  @ContentChild(MultiStringViewDirective) itemViewTpl: MultiStringViewDirective;

  @Input() editMode: boolean = true;

  @Input() validator: ValidatorFn | ValidatorFn[] | null;

  @Input() sequence: string[];

  @Input() textarea: boolean = false;

  @Input() codeInput: boolean = false;

  @Output() sequenceChange = new EventEmitter<string[]>();

  newItemCtrl: FormControl;

  ngOnInit(): void {
    this.newItemCtrl = new FormControl("", this.validator);
  }

  private updateSequence() {
    this.sequence = [...this.sequence];
    this.sequenceChange.emit(this.sequence);
  }

  onItemDrop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.sequence, event.previousIndex, event.currentIndex);
    this.updateSequence();
  }

  addItem() {
    if (!this.newItemCtrl.valid) {
      return;
    }
    if (!this.sequence) {
      this.sequence = [];
    }
    this.sequence.push(this.newItemCtrl.value);
    this.updateSequence();
    this.newItemCtrl.setValue("");
  }

  deleteItem(index: number) {
    this.sequence.splice(index, 1);
    this.updateSequence();
  }

  addItemOnEnter(event: KeyboardEvent) {
    if (!event.shiftKey && this.newItemCtrl.value.indexOf("\n") < 0) {
      this.addItem();
      event.preventDefault();
    }
  }

  saveItem(index: number, newValue: string) {
    this.sequence[index] = newValue;
    this.updateSequence();
  }

  fieldErrors(fc: FormControl): string[] {
    const errors = [];
    if (fc.errors) {
      Object.keys(fc.errors).forEach((k) => errors.push(fc.errors[k]));
    }
    return errors;
  }

  clearNewItem() {
    this.newItemCtrl.setValue("");
    this.newItemCtrl.setErrors(null);
  }
}
