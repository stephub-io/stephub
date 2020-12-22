import { Directive, HostListener } from "@angular/core";
import { InlineEditComponent } from "./inline-edit.component";

@Directive({
  selector: "[saveOnEnter]",
})
export class SaveOnEnterDirective {
  constructor(private editable: InlineEditComponent) {}

  @HostListener("keyup.enter", ["$event"])
  onEnter(event: KeyboardEvent) {
    this.editable.doSave(event);
  }
}
