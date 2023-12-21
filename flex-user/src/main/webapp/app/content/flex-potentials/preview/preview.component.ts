import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { FlexPotentialDTO } from '@app/shared/models';
import { FlexPotentialsService } from '../flex-potentials.service';

@Component({
  selector: 'app-flex-potentials-preview',
  templateUrl: './preview.component.html',
})
export class FlexPotentialsPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: FlexPotentialDTO = this.config?.data;

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private service: FlexPotentialsService
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

  onDownloadFile(id: number): void {
    this.service.downloadFile(id);
  }

  private getData(): void {
    this.service.getFlexPotential(this.versionId).subscribe((response: FlexPotentialDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
