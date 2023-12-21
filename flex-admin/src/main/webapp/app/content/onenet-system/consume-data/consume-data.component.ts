import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SessionStorageService } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { TableExtends } from '@app/shared/services';
import { DescriptionPreviewModalComponent } from '../description-preview-modal/description-preview-modal.component';
import { ConsumeDataDTO } from './consume-data';
import { COLUMNS } from './consume-data.columns';
import { ConsumeDataService } from './consume-data.service';
import { ConsumeDataStore } from './consume-data.store';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-consume-data',
  templateUrl: './consume-data.component.html',
  styleUrls: ['./consume-data.component.scss'],
  providers: [ConsumeDataStore],
})
export class ConsumeDataComponent extends TableExtends implements OnInit {
  viewName = 'consumeData';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'consumeData.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: ConsumeDataStore,
    private service: ConsumeDataService,
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

  preview(row: ConsumeDataDTO): void {
    this.modalService.open(DescriptionPreviewModalComponent, {
      data: {
        id: row.id,
        parentScreenName: this.viewName,
        description: row.description,
      },
    });
  }

  downloadFile(row: ConsumeDataDTO): void {
    this.service.downloadFile(row.onenetId);
  }
}
