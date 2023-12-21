import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { BooleanEnum, Role, VolumeUnit } from '@app/shared/enums';
import { Dictionary, FlexPotentialDTO } from '@app/shared/models';
import { Component, ElementRef, Injector, OnInit, ViewChild } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { FlexPotentialsParameters, FlexPotentialsStore } from './flex-potentials.store';
import { Helpers, ModalService } from '@app/shared/commons';
import { takeUntil, catchError } from 'rxjs/operators';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './flex-potentials.columns';
import { FileUpload } from 'primeng/fileupload';
import { FlexPotentialsDialogComponent } from './dialog';
import { FlexPotentialsPreviewComponent } from './preview';
import { FlexPotentialsService } from './flex-potentials.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { UnitDTO } from '../units/unit';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

interface Dictionaries {
  boolean: Dictionary[];
  volumeUnits: Dictionary[];
}

@Component({
  selector: 'app-flex-potentials',
  templateUrl: './flex-potentials.component.html',
  styleUrls: ['./flex-potentials.component.scss'],
  providers: [ConfirmationService],
})
export class FlexPotentialsComponent extends TableExtends implements OnInit {
  viewName = 'flex-potentials';

  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;

  isRegister = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: FlexPotentialsParameters | undefined;

  seperateFilterDates = ['valid'];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate', 'valid'];

  dictionaries: Dictionaries = {
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
    volumeUnits: Helpers.enumToDictionary(VolumeUnit, 'VolumeUnit'),
  };

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('flexPotentials.actions.export.allData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('flexPotentials.actions.export.displayedData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private authService: AuthService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private store: FlexPotentialsStore,
    private translate: TranslateService,
    private service: FlexPotentialsService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.isRegister = route.snapshot.data?.type === 'REGISTER';

    this.preparedColumnsVisibility();
  }

  add(): void {
    const dialog = this.modalService.open(FlexPotentialsDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: FlexPotentialDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('flexPotentials.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('flexPotentials.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('flexPotentials.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: FlexPotentialDTO): void {
    const dialog = this.modalService.open(FlexPotentialsDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  formatUnits(units: UnitDTO[]): string {
    return units.map((unit: UnitDTO) => `${unit.name}${unit?.sder ? ' (SDER)' : ''}`).join(', ');
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'validFrom', 'validTo'];

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    if (this.isRegister) {
      this.parameters = {
        ...this.parameters,
        'isRegister.equals': this.isRegister,
      };
    }

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  onFileSelect({ currentFiles }: any): void {
    const formData = new FormData();
    formData.append('file', currentFiles[0]);

    this.service
      .import(formData)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(`flexPotentials.actions.import.error`);
            this.fileUploadEl?.clear();
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(`flexPotentials.actions.import.success`);
        this.fileUploadEl?.clear();
      });
  }

  preview(row: FlexPotentialDTO): void {
    this.modalService.open(FlexPotentialsPreviewComponent, { data: row, styleClass: 'full-view' });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData);
  }

  private preparedColumnsVisibility(): void {
    this.authService.hasRole(Role.ROLE_ADMIN).then((hasRole: boolean) => {
      this.columns = this.preparedColumns(COLUMNS, 'flexPotentials.table').filter(({ field }) => {
        if (hasRole) {
          return field !== 'delete' || !this.isRegister;
        }

        return field !== 'delete';
      });
      this.onActiveColumnsChange(false);
    });
  }
}
