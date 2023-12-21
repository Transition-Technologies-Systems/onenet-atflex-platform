import { Directive, HostListener, ElementRef, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appPreventDoubleClick]',
})
export class PreventDoubleClickDirective {
  constructor(private el: ElementRef, private renderer: Renderer2) {}

  @HostListener('click') onClick(): void {
    const button = this.el.nativeElement;
    this.renderer.setStyle(button, 'pointer-events', 'none');
    if (button.disabled) {
      return;
    }
    button.disabled = true;
    setTimeout(() => {
      button.disabled = false;
      this.renderer.setStyle(button, 'pointer-events', 'auto');
    }, 1000);
  }
}