import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { SubportfolioDTO } from '../subportfolio';
import { SubportfoliosService } from '../subportfolio.service';

@Component({
  selector: 'app-subportfolio-preview',
  templateUrl: './preview.component.html',
})
export class SubportfoliosPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: SubportfolioDTO = this.config?.data;

  constructor(
    public ref: DynamicDialogRef,
    private service: SubportfoliosService,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig
  ) {}

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  getCouplingPointIdTypes(row: SubportfolioDTO): string {
    return row.couplingPointIdTypes.map(({ name }) => name).join(', ');
  }

  onDownloadFile(id: number): void {
    this.service.downloadFile(id);
  }

  private getData(): void {
    this.service.getSubportfolio(this.versionId).subscribe((response: SubportfolioDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
