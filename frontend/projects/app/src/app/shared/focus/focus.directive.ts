import { AfterContentInit, Directive, ElementRef, Input } from "@angular/core";

@Directive({
  selector: "[focus]",
})
export class FocusDirective implements AfterContentInit {
  @Input() public focus: boolean;

  public constructor(private el: ElementRef) {}

  public ngAfterContentInit() {
    if (this.focus) {
      setTimeout(() => {
        this.el.nativeElement.focus();
      }, 150);
    }
  }
}
