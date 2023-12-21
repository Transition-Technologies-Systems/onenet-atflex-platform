import { ChangeDetectorRef, Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '@app/core';

@Directive({
  selector: '[appHasRole]',
})
export class HasRoleDirective {
  @Input() appHasRoleContext: object | undefined;
  @Input() appHasRoleElse: TemplateRef<any> | undefined;

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
  set appHasRole(value: string) {
    this.roles = value.split(',');
    this.updateView();
  }

  private updateView(): void {
    this.authService.hasEveryRoles(this.roles).then((hasRole: boolean) => {
      if (this.hasRole !== hasRole && this.templateRef) {
        this.currentRef = undefined;
        this.viewContainerRef.clear();
      }

      if (hasRole && !this.hasRole) {
        if (this.currentRef !== this.templateRef) {
          this.currentRef = this.templateRef;
          this.viewContainerRef.createEmbeddedView(this.templateRef, {
            $implicit: this.appHasRoleContext,
          });
          this.cdr.detectChanges();
        }
      } else if (this.appHasRoleElse && this.currentRef !== this.appHasRoleElse) {
        this.currentRef = this.appHasRoleElse;
        this.viewContainerRef.createEmbeddedView(this.appHasRoleElse, {
          $implicit: this.appHasRoleContext,
        });
        this.cdr.detectChanges();
      }
    });
  }
}
