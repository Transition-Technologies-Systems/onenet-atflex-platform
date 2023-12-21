import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { FspUserRegistrationDTO, Tab, TabType } from './fsp-registration';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './fsp-registration.columns';
import { FspRegistrationPreviewComponent } from './preview';
import { FspRegistrationService } from './fsp-registration.service';
import { FspRegistrationStore } from './fsp-registration.store';
import { SessionStorageService } from '@app/core';
import { ModalService } from '@app/shared/commons';
import { FspUserRegistrationStatus, Role } from '@app/shared/enums';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-fsp-registration',
  templateUrl: './fsp-registration.component.html',
})
export class FspRegistrationComponent extends TableExtends implements OnInit {
  viewName = 'fsp-registration';

  selectedTab: TabType = 'active';

  data$ = this.store.data$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'fspRegistration.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private store: FspRegistrationStore,
    private service: FspRegistrationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  ngOnInit(): void {
    this.getCollection();

    this.route.queryParams.subscribe(data => {
      const previewId = +data.preview;

      if (!!previewId) {
        this.service.getFspRegistration(previewId).subscribe(response => this.preview(response));
      }
    });
  }

  correctStatusForColor(status: FspUserRegistrationStatus): boolean {
    return [
      FspUserRegistrationStatus.NEW,
      FspUserRegistrationStatus.CONFIRMED_BY_FSP,
      FspUserRegistrationStatus.PRE_CONFIRMED_BY_MO,
      FspUserRegistrationStatus.USER_ACCOUNT_ACTIVATED_BY_FSP,
    ].includes(status);
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }

  getCollection(): void {
    this.store.loadCollection({ page: this.page, size: this.rows, sort: this.sort, tabType: this.selectedTab });
  }

  getRoleName(row: FspUserRegistrationDTO): string {
    switch (row.userTargetRole) {
      case Role.ROLE_BALANCING_SERVICE_PROVIDER:
        return this.translate.instant('RoleShort.ROLE_BALANCING_SERVICE_PROVIDER');
      case Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED:
        return this.translate.instant('RoleShort.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED');
    }

    return this.translate.instant('RoleShort.ROLE_FLEX_SERVICE_PROVIDER');
  }

  preview(model: FspUserRegistrationDTO): void {
    const dialog = this.modalService.open(FspRegistrationPreviewComponent, { data: model, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.getCollection();
    });
  }
}
