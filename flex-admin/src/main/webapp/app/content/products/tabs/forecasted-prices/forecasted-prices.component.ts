import { catchError } from 'rxjs';
import * as moment from 'moment';

import { AppToastrService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit, ViewChild } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { ProductsForecastedPricesStore } from './forecasted-prices.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './forecasted-prices.columns';
import { ConfirmationService } from 'primeng/api';
import { FileUpload } from 'primeng/fileupload';
import { ForecastedPricesFileDTO } from './forecasted-prices';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductsForecastedPricesPreviewComponent } from './preview';
import { ProductsForecastedPricessService } from './forecasted-prices.service';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { CustomConfirmComponent } from '@app/shared/commons/custom-confirm/custom-confirm.component';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-products-forecasted-prices',
  templateUrl: './forecasted-prices.component.html',
  providers: [ConfirmationService, ProductsForecastedPricesStore],
})
export class ProductsForecastedPricesComponent extends TableExtends implements OnInit {
  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;

  viewName = 'products-forecasted-prices';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'products.forecastedPrices.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private store: ProductsForecastedPricesStore,
    private service: ProductsForecastedPricessService,
    private confirmationService: ConfirmationService,
    private customConfirmComponent: CustomConfirmComponent,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  delete(event: Event, row: ForecastedPricesFileDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('products.actions.forecastedPrices.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('products.actions.forecastedPrices.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('products.actions.forecastedPrices.delete.success');
            this.getCollection();
          });
      },
    });
  }

  download(): void {
    this.service.downloadTemplate();
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'forecastedPricesDate'];

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

  import({ currentFiles }: any): void {
    const formData = new FormData();
    currentFiles.forEach((_: File, index: number) => {
      formData.append('file', currentFiles[index]);
    });

    const importForecastedPrices = (force: boolean = false) =>
      this.service
        .import(formData, force)
        .pipe(
          catchError((response: HttpErrorResponse): any => {
            const errorParams = response.error.params ? JSON.parse(response.error.params) : {};
            const { invalidFiles, invalidForecastedPrices } = errorParams;

            if (!force && response.status === 400 && response.error?.errorKey === 'DUPLICATE_FORECASTED_PRICES') {
              this.confirmationService.confirm({
                message: this.customConfirmComponent.createItemListMessage(
                  this.translate.instant('products.actions.forecastedPrices.import.reimportQuestion'),
                  invalidForecastedPrices?.DUPLICATE_FORECASTED_PRICES.map(({ productName, forecastedPricesDate }: any) => {
                    return `${productName} ${moment(forecastedPricesDate).format('DD/MM/YYYY')}`;
                  })
                ),
                header: this.translate.instant('products.actions.forecastedPrices.import.questionHeader'),
                accept: () => importForecastedPrices(true),
                reject: () => this.fileUploadEl?.clear(),
                key: 'confirm-dialog',
              });

              return;
            }

            this.fileUploadEl?.clear();

            if (response.status === 400 && response.error?.errorKey) {
              for (const property in invalidFiles) {
                if (property) {
                  const message = this.translate.instant(property, { files: invalidFiles[property] });
                  this.toastr.error(message);
                }
              }

              if (invalidForecastedPrices?.SAVING_THE_SAME_FORECASTED_PRICES) {
                const files = invalidForecastedPrices.SAVING_THE_SAME_FORECASTED_PRICES.map(
                  ({ productName, forecastedPricesDate }: any) => {
                    return `${productName} ${moment(forecastedPricesDate).format('DD/MM/YYYY')}`;
                  }
                ).join(', ');
                const message = this.translate.instant(`error.forecastedPrices.SAVING_THE_SAME_FORECASTED_PRICES`, {
                  files,
                });
                this.toastr.error(message);
              }
              return;
            }

            this.toastr.error(`products.actions.forecastedPrices.import.error`);
          })
        )
        .subscribe(() => {
          this.toastr.success(`products.actions.forecastedPrices.import.success`);
          this.fileUploadEl?.clear();
          this.getCollection();
        });

    importForecastedPrices(false);
  }

  preview(row: ForecastedPricesFileDTO): void {
    this.modalService.open(ProductsForecastedPricesPreviewComponent, { data: row, styleClass: 'full-view' });
  }
}
