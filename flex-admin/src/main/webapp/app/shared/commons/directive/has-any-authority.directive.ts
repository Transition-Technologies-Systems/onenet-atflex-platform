import { ChangeDetectorRef, Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';

import { AuthService } from '@app/core';

@Directive({
  selector: '[appHasAnyAuthority]',
})
export class HasAnyAuthorityDirective {
  @Input() appHasAnyAuthorityContext: object | undefined;
  @Input() appHasAnyAuthorityElse: TemplateRef<any> | undefined;

  private currentRef: TemplateRef<any> | undefined;
  private hasAuthority = false;
  private authority: string[] = [];

  constructor(
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private templateRef: TemplateRef<any>,
    private viewContainerRef: ViewContainerRef
  ) {}

  @Input()
  set appHasAnyAuthority(value: string | undefined) {
    this.authority = value ? value.split(',') : [];
    this.updateView();
  }

  private updateView(): void {
    this.authService.hasAnyAuthority(this.authority).then((hasAuthority: boolean) => {
      if (this.hasAuthority !== hasAuthority && this.templateRef) {
        this.currentRef = undefined;
        this.viewContainerRef.clear();
      }

      if (hasAuthority && !this.hasAuthority) {
        if (this.currentRef !== this.templateRef) {
          this.currentRef = this.templateRef;
          this.viewContainerRef.createEmbeddedView(this.templateRef, {
            $implicit: this.appHasAnyAuthorityContext,
          });
          this.cdr.detectChanges();
        }
      } else if (this.appHasAnyAuthorityElse && this.currentRef !== this.appHasAnyAuthorityElse) {
        this.currentRef = this.appHasAnyAuthorityElse;
        this.viewContainerRef.createEmbeddedView(this.appHasAnyAuthorityElse, {
          $implicit: this.appHasAnyAuthorityContext,
        });
        this.cdr.detectChanges();
      }
    });
  }
}
