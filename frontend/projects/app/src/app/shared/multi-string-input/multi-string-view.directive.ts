import { Directive, TemplateRef } from "@angular/core";

@Directive({
  selector: "[multiStringView]",
})
export class MultiStringViewDirective {
  constructor(public tpl: TemplateRef<any>) {}
}
