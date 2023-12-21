import { Directive, TemplateRef } from '@angular/core';

/**
 * Directive to declare a template for filter
 */
@Directive({
  selector: '[appFilterTemplate]',
})
export class FilterTemplateDirective {
  constructor(public template: TemplateRef<any>) {}
}
