import { Directive, HostListener } from "@angular/core";
import { InlineEditComponent } from "./inline-edit.component";

@Directive({
  selector: "[cancelOnEsc]",
})
export class CancelOnEscDirective {
  constructor(private editable: InlineEditComponent) {}

  @HostListener("keyup.esc", ["$event"])
  onEsc(event: KeyboardEvent) {
    this.editable.doCancel(event);
  }
}
