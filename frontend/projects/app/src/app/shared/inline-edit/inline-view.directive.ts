import { Directive, TemplateRef } from "@angular/core";

@Directive({
  selector: "[inlineView]",
})
export class InlineViewDirective {
  constructor(public tpl: TemplateRef<any>) {}
}
