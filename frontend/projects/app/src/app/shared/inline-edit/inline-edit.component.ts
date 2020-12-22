import {
  ChangeDetectionStrategy,
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from "@angular/core";
import { InlineViewDirective } from "./inline-view.directive";
import { InlineEditDirective } from "./inline-edit.directive";
import { BehaviorSubject, fromEvent, Subject } from "rxjs";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { filter, switchMapTo, take } from "rxjs/operators";
import { FormControl, ValidatorFn } from "@angular/forms";

@Component({
  selector: "sh-inline-edit",
  templateUrl: "./inline-edit.component.html",
  styleUrls: ["./inline-edit.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
@UntilDestroy()
export class InlineEditComponent implements OnInit {
  @Output() save = new EventEmitter();
  @Output() cancel = new EventEmitter();
  @ContentChild(InlineViewDirective) viewModeTpl: InlineViewDirective;
  @ContentChild(InlineEditDirective) editModeTpl: InlineEditDirective;
  @Input() value: any;
  @Input() validator: ValidatorFn | ValidatorFn[] | null;

  mode = new BehaviorSubject("view");
  mode$ = this.mode.asObservable();

  editMode = new Subject();
  editMode$ = this.editMode.asObservable();

  formControl: FormControl;

  constructor(private host: ElementRef) {}

  ngOnInit() {
    this.viewModeHandler();
    this.editModeHandler();
  }

  toViewMode(doSave: boolean) {
    if (doSave) {
      this.save.next(this.formControl.value);
    } else {
      this.cancel.next();
    }
    this.mode.next("view");
  }

  private get element() {
    return this.host.nativeElement;
  }

  private viewModeHandler() {
    fromEvent(this.element, "click")
      .pipe(untilDestroyed(this))
      .subscribe((event: MouseEvent) => {
        if (this.mode.value != "edit") {
          event.stopPropagation();
          this.formControl = new FormControl(this.value, this.validator);
          this.mode.next("edit");
          this.editMode.next(true);
        }
      });
  }

  private editModeHandler() {
    const clickOutside$ = fromEvent(document, "click").pipe(
      filter(({ target }) => this.element.contains(target) === false),
      take(1)
    );

    this.editMode$
      .pipe(switchMapTo(clickOutside$), untilDestroyed(this))
      .subscribe((event) => {
        if (this.mode.value != "view") {
          this.doSave(event);
        }
      });
  }

  ngOnDestroy() {}

  doSave(event) {
    event.stopPropagation();
    if (!this.formControl.errors) {
      this.toViewMode(true);
    }
  }

  doCancel(event) {
    event.stopPropagation();
    this.toViewMode(false);
  }
}
