import { ComponentRef, Injectable, Type, ViewRef } from '@angular/core';
import { DialogService, DynamicDialogComponent, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { DomHandler } from 'primeng/dom';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';

@Injectable()
export class ModalService {
  constructor(private router: Router, private dialogService: DialogService) {}

  open(component: Type<any>, configuration: DynamicDialogConfig = {}): DynamicDialogRef {
    const dialogRef = this.dialogService.open(component, {
      showHeader: false,
      contentStyle: { 'min-height': '100px', 'max-height': '96vh', overflow: 'auto', background: 'transparent', padding: 0 },
      baseZIndex: 0,
      ...configuration,
      styleClass: configuration.styleClass ? `app-modal ${configuration.styleClass}` : 'app-modal',
    });

    const componentRef: ComponentRef<DynamicDialogComponent> | undefined = this.dialogService.dialogComponentRefMap.get(dialogRef);

    if (!!componentRef) {
      const component: DynamicDialogComponent = componentRef.instance;

      component.disableModality = () => {
        if (component.wrapper) {
          const opened = this.dialogService.dialogComponentRefMap.size;

          if (component.config.dismissableMask) {
            component.unbindMaskClickListener();
          }

          if (component.config.modal !== false && opened === 0) {
            DomHandler.removeClass(document.body, 'p-overflow-hidden');
          }

          if (!((component as any).cd as ViewRef).destroyed) {
            (component as any).cd.detectChanges();
          }
        }
      };
    }

    this.router.events.pipe(first()).subscribe(() => dialogRef.close());

    return dialogRef;
  }
}
