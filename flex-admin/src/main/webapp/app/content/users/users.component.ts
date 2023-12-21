import { AppToastrService, SessionStorageService } from '@app/core';
import { BooleanEnum, Role } from '@app/shared/enums';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { Dictionary, UserDTO } from '@app/shared/models';
import { Helpers, ModalService } from '@app/shared/commons';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './users.columns';
import { ConfirmationService } from 'primeng/api';
import { HttpErrorResponse } from '@angular/common/http';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { UsersDialogComponent } from './dialog';
import { UsersService } from './users.service';
import { UsersStore } from './users.store';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { catchError, takeUntil } from 'rxjs/operators';

interface Dictionaries {
  roles: Dictionary[];
  boolean: Dictionary[];
}

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  providers: [ConfirmationService],
})
export class UsersComponent extends TableExtends implements OnInit {
  viewName = 'users';

  data$ = this.store.data$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'users.table');

  dictionaries: Dictionaries = {
    roles: Helpers.enumToDictionary(Role, 'RoleShort'),
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
  };

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: UsersStore,
    private service: UsersService,
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
    const dialog = this.modalService.open(UsersDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: UserDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('users.actions.delete.question'),
      acceptLabel: this.translate.instant('users.actions.delete.answerYes'),
      rejectLabel: this.translate.instant('users.actions.delete.answerNo'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('users.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('users.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: UserDTO): void {
    const dialog = this.modalService.open(UsersDialogComponent, { data: row, styleClass: 'full-view' });

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
      runAfterGetData: () => this.updateHandyScroll(),
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters),
    });
  }

  preview(row: UserDTO): void {}
}
