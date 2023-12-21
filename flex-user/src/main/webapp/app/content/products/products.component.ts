import { BooleanEnum, ProductDirection, VolumeUnit } from '@app/shared/enums';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { DefaultParameters, Dictionary, ProductDTO } from '@app/shared/models';
import { Helpers, ModalService } from '@app/shared/commons';
import { ProductsStore } from './products.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './products.columns';
import { SessionStorageService } from '@app/core';
import { MenuItem } from 'primeng/api';
import { ProductsPreviewComponent } from './preview';
import { ProductsService } from './products.service';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

interface Dictionaries {
  units: Dictionary[];
  boolean: Dictionary[];
  directions: Dictionary[];
}

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
})
export class ProductsComponent extends TableExtends implements OnInit {
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
    private store: ProductsStore,
    private service: ProductsService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
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
