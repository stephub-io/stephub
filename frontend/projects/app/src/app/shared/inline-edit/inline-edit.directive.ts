import { Directive, TemplateRef } from "@angular/core";

@Directive({
  selector: "[inlineEdit]",
})
export class InlineEditDirective {
  constructor(public tpl: TemplateRef<any>) {}
}
