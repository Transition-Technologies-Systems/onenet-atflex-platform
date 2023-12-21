import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppToastrService, SessionStorageService } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { COLUMNS } from './ons-users.columns';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService } from 'primeng/api';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { TableExtends } from '@app/shared/services';
import { OnsUsersService } from './ons-users.service';
import { OnsUsersStore } from './ons-users.store';
import { OnsUserDTO } from './ons-users';
import { OnsUsersDialogComponent } from './ons-users-dialog/ons-users-dialog.component';
import { takeUntil, catchError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-ons-users',
  templateUrl: './ons-users.component.html',
  styleUrls: ['./ons-users.component.scss'],
  providers: [OnsUsersStore],
})
export class OnsUsersComponent extends TableExtends implements OnInit {
  viewName = 'onsUsers';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'onsUsers.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: OnsUsersStore,
    private service: OnsUsersService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  getCollection(): void {
    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getRowClass(row: OnsUserDTO): string {
    if (row.active) {
      return 'user-active';
    }
    return '';
  }

  setActiveUser(id: number) {
    this.service
      .setActiveUser(id)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error('onsUsers.actions.setActiveUser.error');
            this.getCollection();
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success('onsUsers.actions.setActiveUser.success');
        this.getCollection();
      });
  }

  add(): void {
    const dialog = this.modalService.open(OnsUsersDialogComponent, { styleClass: 'full-view' });
    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: OnsUserDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('onsUsers.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('onsUsers.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('onsUsers.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }
}
