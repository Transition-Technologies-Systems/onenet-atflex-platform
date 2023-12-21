import { MenuItem } from 'primeng/api';

import { Component, OnInit } from '@angular/core';

import { DocumentDTO, DOWNLOAD_ICON } from './manuals.models';
import { ManualsService } from './manuals.service';

@Component({
  selector: 'app-manuals-download',
  template: `<button
      appPreventDoubleClick
      pButton
      pRipple
      type="button"
      [pTooltip]="'docsDownload.info' | translate"
      [label]="'docsDownload.documents' | translate"
      (click)="exportMenu.toggle($event)"
    ></button>
    <p-menu #exportMenu [popup]="true" [model]="exportOptions" appendTo="body"></p-menu>`,
  styleUrls: ['./manuals-download.component.scss'],
  providers: [ManualsService],
})
export class ManualsDownloadComponent implements OnInit {
  exportOptions: MenuItem[] = [];

  constructor(private service: ManualsService) {
    this.subscribeManualFilesList();
  }

  ngOnInit() {}

  private downloadDoc = (event: any) => {
    this.service.downloadDoc(event);
  };

  private subscribeManualFilesList() {
    this.service
      .getManualFilesList()
      .subscribe(
        (data: DocumentDTO[]) => (this.exportOptions = data.map(doc => ({ label: doc.id, icon: DOWNLOAD_ICON, command: this.downloadDoc })))
      );
  }
}
