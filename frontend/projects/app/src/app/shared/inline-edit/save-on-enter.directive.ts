import { Directive, ElementRef, HostListener, Input } from "@angular/core";
import { InlineEditComponent } from "./inline-edit.component";

@Directive({
  selector: "[saveOnEnter]",
})
export class SaveOnEnterDirective {
  @Input() public saveOnEnter: boolean = true;

  constructor(
    private host: ElementRef,
    private editable: InlineEditComponent
  ) {}

  @HostListener("keydown.enter", ["$event"])
  onEnter(event: KeyboardEvent) {
    if (
      this.saveOnEnter &&
      !event.shiftKey &&
      (this.host.nativeElement as HTMLInputElement).value.indexOf("\n") < 0
    ) {
      this.editable.doSave(event);
      event.preventDefault();
    }
  }
}
