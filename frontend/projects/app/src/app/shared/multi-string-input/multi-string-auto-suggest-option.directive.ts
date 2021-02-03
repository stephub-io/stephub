import { Directive, TemplateRef } from "@angular/core";

@Directive({
  selector: "[multiStringAutoSuggestOption]",
})
export class MultiStringAutoSuggestOptionDirective {
  constructor(public tpl: TemplateRef<any>) {}
}
