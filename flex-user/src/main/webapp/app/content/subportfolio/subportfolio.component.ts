import { AppToastrService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { DefaultParameters, Dictionary, LocalizationTypeDTO } from '@app/shared/models';
import { DirectionOfDeviationType, LocalizationType } from '@app/shared/enums';
import { Helpers, ModalService } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { SubportfoliosStore } from './subportfolio.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './subportfolio.columns';
import { Observable } from 'rxjs';
import { ROLES_WITH_FSP } from '@app/app.config';
import { SubportfolioDTO } from './subportfolio';
import { SubportfoliosDialogComponent } from './dialog';
import { SubportfoliosPreviewComponent } from './preview/preview.component';
import { SubportfoliosService } from './subportfolio.service';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs/operators';

interface Dictionaries {
  directions: Dictionary[];
  localizationTypes$: Observable<LocalizationTypeDTO[]>;
}

@Component({
  selector: 'app-subportfolio',
  templateUrl: './subportfolio.component.html',
  styleUrls: ['./subportfolio.component.scss'],
  providers: [ConfirmationService],
})
export class SubportfoliosComponent extends TableExtends implements OnInit {
  viewName = 'subportfolio';

  apiLoaded = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  mangeRoles = ROLES_WITH_FSP;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'subportfolio.table');

  dictionaries: Dictionaries = {
    directions: Helpers.enumToDictionary(DirectionOfDeviationType, 'DirectionOfDeviationType'),
    localizationTypes$: this.service.getLocalizationsDict(LocalizationType.COUPLING_POINT_ID),
  };

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('subportfolio.actions.export.allData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('subportfolio.actions.export.displayedData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: SubportfoliosStore,
    private service: SubportfoliosService,
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
    const dialog = this.modalService.open(SubportfoliosDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: SubportfolioDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('subportfolio.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('subportfolio.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('subportfolio.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: SubportfolioDTO): void {
    const dialog = this.modalService.open(SubportfoliosDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  getCouplingPointIdTypes(row: SubportfolioDTO): string {
    return row.couplingPointIdTypes.map(({ name }) => name).join(', ');
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'validFrom', 'validTo'];

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

  preview(row: SubportfolioDTO): void {
    this.modalService.open(SubportfoliosPreviewComponent, { data: row, styleClass: 'full-view' });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData);
  }
}
