import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivationsSettlementsDTO } from '@app/shared/models/activations-settlements';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ActivationsSettlementsService } from '../activations-settlements.service';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
})
export class ActivationsSettlementsPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: ActivationsSettlementsDTO = this.config?.data;

  hasRole = false;

  constructor(
    public ref: DynamicDialogRef,
    private service: ActivationsSettlementsService,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig
  ) {}

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  formatDeliveryDate(row: ActivationsSettlementsDTO): string {
    return this.service.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  close(): void {
    this.ref.close();
  }

  showTooltip(row: ActivationsSettlementsDTO): string {
    if (row.acceptedVolumeTooltipVisible) {
      return 'shared.volumeTooltip';
    } else if (row.acceptedVolumeCmvcTooltipVisible) {
      return 'activationsSettlements.table.tooltip.acceptedVolumeCmvcTooltip';
    }
    return '';
  }

  private getData(): void {
    this.service.getActivationSettlement(this.versionId).subscribe((response: ActivationsSettlementsDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
