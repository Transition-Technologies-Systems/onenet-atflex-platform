import { AppToastrService, SessionStorageService } from '@app/core';
import { BooleanEnum, ProductDirection, VolumeUnit } from '@app/shared/enums';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { DefaultParameters, Dictionary, ProductDTO } from '@app/shared/models';
import { Helpers, ModalService } from '@app/shared/commons';
import { ProductsListStore } from './list.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './list.columns';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductsDialogComponent } from '../../dialog';
import { ProductsPreviewComponent } from '../../preview';
import { ProductsService } from '../../products.service';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { catchError, takeUntil } from 'rxjs/operators';

interface Dictionaries {
  units: Dictionary[];
  boolean: Dictionary[];
  directions: Dictionary[];
}

@Component({
  selector: 'app-products-list',
  templateUrl: './list.component.html',
  providers: [ConfirmationService, ProductsListStore],
})
export class ProductsListComponent extends TableExtends implements OnInit {
  viewName = 'products';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'products.table');

  dictionaries: Dictionaries = {
    units: Helpers.enumToDictionary(VolumeUnit, 'VolumeUnit'),
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
    directions: Helpers.enumToDictionary(ProductDirection, 'Direction'),
  };

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('products.actions.export.allData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('products.actions.export.displayedData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: ProductsListStore,
    private service: ProductsService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  add(): void {
    const dialog = this.modalService.open(ProductsDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: ProductDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('products.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('products.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('products.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: ProductDTO): void {
    const dialog = this.modalService.open(ProductsDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate'].concat(Helpers.createKeys(['valid'], ['from', 'to']));

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  preview(row: ProductDTO): void {
    this.modalService.open(ProductsPreviewComponent, { data: row, styleClass: 'full-view' });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData);
  }
}
