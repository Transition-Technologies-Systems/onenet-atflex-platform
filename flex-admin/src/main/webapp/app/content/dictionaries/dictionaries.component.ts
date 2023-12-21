import { AppToastrService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { BooleanEnum, DerType, DictionaryType, LocalizationType } from '@app/shared/enums';
import { Helpers, ModalService } from '@app/shared/commons';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './dictionaries.columns';
import { ConfirmationService } from 'primeng/api';
import { DictDialogComponent } from './dialog/dialog.component';
import { DictPreviewComponent } from './preview/preview.component';
import { DictionariesService } from './dictionaries.service';
import { DictionariesStore } from './dictionaries.store';
import { Dictionary } from '@app/shared/models';
import { DictionaryLangDto } from './dictionaries';
import { HttpErrorResponse } from '@angular/common/http';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs';
import { KdmModelsDialogComponent } from './kdm-models-dialog/kdm-models-dialog.component';

interface Dictionaries {
  localizationTypes: Dictionary[];
  derTypes: Dictionary[];
  boolean: Dictionary[];
}

@Component({
  selector: 'app-dictionaries',
  templateUrl: './dictionaries.component.html',
  providers: [ConfirmationService],
})
export class DictionariesComponent extends TableExtends implements OnInit {
  viewName = 'dictionaries-page';
  dictionaryType: DictionaryType;

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'dictionariesPage.table');

  dictionaries: Dictionaries = {
    localizationTypes: Helpers.enumToDictionary(LocalizationType, 'LocalizationType'),
    derTypes: Helpers.enumToDictionary(DerType, 'DerType'),
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
  };

  get isDerType(): boolean {
    return this.dictionaryType === DictionaryType.DER_TYPE;
  }

  private permissionPrefix = '';

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: DictionariesStore,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private service: DictionariesService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.dictionaryType = route.snapshot.data.type;
    this.permissionPrefix = `FLEX_ADMIN_${this.dictionaryType}_`;
    this.service.setDictionaryType(this.dictionaryType);

    this.viewName = 'dictionaries-page-' + this.dictionaryType.toLowerCase();

    this.columns = this.preparedColumns(COLUMNS, 'dictionariesPage.table').filter(({ field }) => {
      if (this.dictionaryType === DictionaryType.DER_TYPE) {
        return !['products', 'name', 'areaName', 'lvModel', 'action'].includes(field);
      }

      if (this.dictionaryType === DictionaryType.SCHEDULING_UNIT_TYPE) {
        return !['sderPoint', 'name', 'type', 'areaName', 'lvModel', 'action'].includes(field);
      }

      if (this.dictionaryType === DictionaryType.LOCALIZATION_TYPE) {
        return !['products', 'sderPoint', 'descriptionPl', 'descriptionEn', 'areaName', 'lvModel', 'action'].includes(field);
      }

      if (this.dictionaryType === DictionaryType.KDM_MODEL) {
        return !['products', 'name', 'sderPoint', 'type', 'descriptionPl', 'descriptionEn'].includes(field);
      }

      return true;
    });

    this.onActiveColumnsChange(false);
  }

  add(): void {
    const dialog = this.modalService.open(DictDialogComponent, { styleClass: 'full-view', data: { type: this.dictionaryType } });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: DictionaryLangDto): void {
    const isType = [DictionaryType.DER_TYPE, DictionaryType.SCHEDULING_UNIT_TYPE].includes(this.dictionaryType);
    const typeKey = this.dictionaryType.toLocaleLowerCase();
    const key = isType ? 'delete' : `delete.${typeKey}`;

    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant(`dictionariesPage.actions.${key}.question`),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error(`dictionariesPage.actions.${key}.error`);
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success(`dictionariesPage.actions.${key}.success`);
            this.getCollection();
          });
      },
    });
  }

  edit(row: DictionaryLangDto): void {
    const dialog = this.modalService.open(DictDialogComponent, {
      data: {
        model: row,
        type: this.dictionaryType,
      },
      styleClass: 'full-view',
    });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  getCollection(): void {
    this.store.loadCollection({
      page: this.page,
      size: this.rows,
      sort: this.sort,
      type: this.dictionaryType,
      runAfterGetData: () => this.updateHandyScroll(),
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['createdDate', 'lastModifiedDate']),
    });
  }

  getProductValues(row: DictionaryLangDto): string {
    const products = row.products ?? [];

    return products.map(({ shortName }) => shortName).join(', ');
  }

  preview(row: DictionaryLangDto): void {
    this.modalService.open(DictPreviewComponent, {
      data: {
        model: row,
        dictionaryType: this.dictionaryType,
      },
      styleClass: 'full-view',
    });
  }

  getPermission(type: 'VIEW' | 'MANAGE' | 'DELETE'): string {
    return `${this.permissionPrefix}${type}`;
  }

  addKdmModelForArea(row: DictionaryLangDto) {
    const dialog = this.modalService.open(KdmModelsDialogComponent, {
      data: {
        model: row,
      },
      styleClass: 'full-view',
    });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }
}
