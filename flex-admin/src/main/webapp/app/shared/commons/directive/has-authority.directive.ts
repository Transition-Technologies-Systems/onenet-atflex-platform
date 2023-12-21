import { ChangeDetectorRef, Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '@app/core';

@Directive({
  selector: '[appHasAuthority]',
})
export class HasAuthorityDirective {
  @Input() appHasAuthorityContext: object | undefined;
  @Input() appHasAuthorityElse: TemplateRef<any> | undefined;

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
  set appHasAuthority(value: string) {
    this.authority = value.split(',');
    this.updateView();
  }

  private updateView(): void {
    const promisses = this.authority.map((authority: string) => this.authService.hasAuthority(authority));

    Promise.all(promisses).then((authorites: boolean[]) => {
      const hasAuthority = authorites.every(Boolean);

      if (this.hasAuthority !== hasAuthority && this.templateRef) {
        this.currentRef = undefined;
        this.viewContainerRef.clear();
      }

      if (hasAuthority && !this.hasAuthority) {
        if (this.currentRef !== this.templateRef) {
          this.currentRef = this.templateRef;
          this.viewContainerRef.createEmbeddedView(this.templateRef, {
            $implicit: this.appHasAuthorityContext,
          });
          this.cdr.detectChanges();
        }
      } else if (this.appHasAuthorityElse && this.currentRef !== this.appHasAuthorityElse) {
        this.currentRef = this.appHasAuthorityElse;
        this.viewContainerRef.createEmbeddedView(this.appHasAuthorityElse, {
          $implicit: this.appHasAuthorityContext,
        });
        this.cdr.detectChanges();
      }
    });
  }
}
