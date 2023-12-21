import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SessionStorageService } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { TableExtends } from '@app/shared/services';
import { DescriptionPreviewModalComponent } from '../description-preview-modal/description-preview-modal.component';
import { ProvideDialogComponent } from '../provide-dialog/provide-dialog.component';
import { ProvideDataDTO } from './provide-data';
import { COLUMNS } from './provide-data.columns';
import { ProvideDataService } from './provide-data.service';
import { ProvideDataStore } from './provide-data.store';
import { takeUntil } from 'rxjs';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-provide-data',
  templateUrl: './provide-data.component.html',
  styleUrls: ['./provide-data.component.scss'],
  providers: [ProvideDataStore],
})
export class ProvideDataComponent extends TableExtends implements OnInit {
  viewName = 'provideData';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'provideData.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: ProvideDataStore,
    private service: ProvideDataService,
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

  preview(row: ProvideDataDTO): void {
    this.modalService.open(DescriptionPreviewModalComponent, {
      data: {
        id: row.id,
        parentScreenName: this.viewName,
        description: row.description,
      },
    });
  }

  downloadFile(row: ProvideDataDTO): void {
    if (row.fileAvailable) {
      this.service.downloadFile(row.id);
    }
  }

  provide(): void {
    const dialog = this.modalService.open(ProvideDialogComponent, {
      styleClass: 'full-view',
      data: { selectDefaultAndDisableOptions: true },
    });
    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }
}
