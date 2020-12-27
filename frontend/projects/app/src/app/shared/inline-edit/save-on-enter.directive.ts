import { Directive, ElementRef, HostListener } from "@angular/core";
import { InlineEditComponent } from "./inline-edit.component";

@Directive({
  selector: "[saveOnEnter]",
})
export class SaveOnEnterDirective {
  constructor(
    private host: ElementRef,
    private editable: InlineEditComponent
  ) {}

  @HostListener("keydown.enter", ["$event"])
  onEnter(event: KeyboardEvent) {
    if (
      !event.shiftKey &&
      (this.host.nativeElement as HTMLInputElement).value.indexOf("\n") < 0
    ) {
      this.editable.doSave(event);
      event.preventDefault();
    }
  }
}
