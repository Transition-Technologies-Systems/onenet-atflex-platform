import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SessionStorageService } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { TableExtends } from '@app/shared/services';
import { ProvideDialogComponent } from '../provide-dialog/provide-dialog.component';
import { OfferedServicesDTO } from './offered-services';
import { COLUMNS } from './offered-services.columns';
import { OfferedServicesService } from './offered-services.service';
import { OfferedServicesStore } from './offered-services.store';
import { takeUntil } from 'rxjs';
import { DescriptionPreviewModalComponent } from '../description-preview-modal/description-preview-modal.component';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-offered-services',
  templateUrl: './offered-services.component.html',
  styleUrls: ['./offered-services.component.scss'],
  providers: [OfferedServicesStore],
})
export class OfferedServicesComponent extends TableExtends implements OnInit {
  viewName = 'offeredServices';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'offeredServices.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: OfferedServicesStore,
    private service: OfferedServicesService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
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

  preview(row: OfferedServicesDTO): void {
    this.modalService.open(DescriptionPreviewModalComponent, {
      data: {
        id: row.id,
        parentScreenName: this.viewName,
        description: row.description,
      },
    });
  }

  downloadFile(row: OfferedServicesDTO, isSchema: boolean): void {
    this.service.downloadFile(row.id, isSchema);
  }

  provide(row: OfferedServicesDTO): void {
    const dialog = this.modalService.open(ProvideDialogComponent, { styleClass: 'full-view', data: row });
    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }
}
