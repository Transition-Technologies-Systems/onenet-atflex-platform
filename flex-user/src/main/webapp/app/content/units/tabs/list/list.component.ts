import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit, AfterViewInit } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { DirectionOfDeviationType } from '@app/shared/enums';
import { Helpers, ModalService } from '@app/shared/commons';
import { UnitsListStore } from './list.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './list.columns';
import { DefaultParameters, Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { ROLES_WITH_FSP } from '@app/app.config';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { UnitDTO } from '../../unit';
import { UnitsDialogComponent } from '../../dialog';
import { UnitsInviteDerComponent } from '../../invite-der';
import { UnitsPreviewComponent } from '../../preview';
import { UnitsService } from '../../units.service';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs/operators';
import { RxStompService } from '@stomp/ng2-stompjs';

interface Dictionaries {
  directions: Dictionary[];
}

@Component({
  selector: 'app-units-list',
  templateUrl: './list.component.html',
  providers: [ConfirmationService, UnitsListStore],
})
export class UnitsListComponent extends TableExtends implements OnInit, AfterViewInit {
  viewName = 'units';

  isBsp = false;
  apiLoaded = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  mangeRoles = ROLES_WITH_FSP;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'units.table');

  dictionaries: Dictionaries = {
    directions: Helpers.enumToDictionary(DirectionOfDeviationType, 'DirectionOfDeviationType'),
  };

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('units.actions.export.allData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('units.actions.export.displayedData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: UnitsListStore,
    private service: UnitsService,
    private authService: AuthService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    private rxStompService: RxStompService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.checkRole();
  }

  ngAfterViewInit(): void {
    this.afterViewInit();
    this.watchRxStomp();
  }

  add(): void {
    const dialog = this.modalService.open(UnitsDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: UnitDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('units.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('units.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('units.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: UnitDTO): void {
    const dialog = this.modalService.open(UnitsDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
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

  getDerType(row: UnitDTO): string {
    return this.service.getDerType(row);
  }

  preview(row: UnitDTO): void {
    this.modalService.open(UnitsPreviewComponent, { data: row, styleClass: 'full-view' });
  }

  proposal(row: UnitDTO): void {
    this.modalService.open(UnitsInviteDerComponent, {
      data: {
        id: row.id,
      },
    });
  }

  private checkRole(): void {
    this.authService.hasRole('ROLE_BALANCING_SERVICE_PROVIDER').then((hasRole: boolean) => {
      this.isBsp = hasRole;
    });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData);
  }

  private watchRxStomp(): void {
    this.rxStompService
      .watch('/refresh-view/unit')
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: UnitDTO = JSON.parse(message.body || '');

        if (!!data && data.id) {
          this.store.upsertOne(data);
        }
      });
  }
}
