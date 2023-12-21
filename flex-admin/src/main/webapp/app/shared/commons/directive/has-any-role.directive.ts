import { ChangeDetectorRef, Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '@app/core';

@Directive({
  selector: '[appHasAnyRole]',
})
export class HasAnyRoleDirective {
  @Input() appHasAnyRoleContext: object | undefined;
  @Input() appHasAnyRoleElse: TemplateRef<any> | undefined;

  private currentRef: TemplateRef<any> | undefined;
  private roles: string[] = [];
  private hasRole = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private templateRef: TemplateRef<any>,
    private viewContainerRef: ViewContainerRef
  ) {}

  @Input()
  set appHasAnyRole(value: string) {
    this.roles = value.split(',');
    this.updateView();
  }

  private updateView(): void {
    this.authService.hasAnyRoles(this.roles).then((hasRole: boolean) => {
      if (this.hasRole !== hasRole && this.templateRef) {
        this.currentRef = undefined;
        this.viewContainerRef.clear();
      }

      if (hasRole && !this.hasRole) {
        if (this.currentRef !== this.templateRef) {
          this.currentRef = this.templateRef;
          this.viewContainerRef.createEmbeddedView(this.templateRef, {
            $implicit: this.appHasAnyRoleContext,
          });
          this.cdr.detectChanges();
        }
      } else if (this.appHasAnyRoleElse && this.currentRef !== this.appHasAnyRoleElse) {
        this.currentRef = this.appHasAnyRoleElse;
        this.viewContainerRef.createEmbeddedView(this.appHasAnyRoleElse, {
          $implicit: this.appHasAnyRoleContext,
        });
        this.cdr.detectChanges();
      }
    });
  }
}
