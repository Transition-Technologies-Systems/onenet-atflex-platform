import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SelfScheduleDetailDTO, SelfScheduleFileDTO } from '../self-schedule';

import { UnitsSelfSchedulesService } from '../self-schedules.service';

@Component({
  selector: 'app-units-self-schedule-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss'],
})
export class UnitsSelfSchedulePreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: SelfScheduleFileDTO = this.config?.data;

  details: SelfScheduleDetailDTO[] = [];
  hours: string[] = Array.from({ length: 25 }, (_, i: number) => (i === 24 ? '2a' : `${i + 1}`));

  constructor(private service: UnitsSelfSchedulesService, public config: DynamicDialogConfig, public ref: DynamicDialogRef) {}

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  private getData(): void {
    this.service.getSelfScheduleDetail(this.data.id).subscribe(response => {
      if (response) {
        this.hours = response.volumes.map(({ id }) => id);
      }

      this.details = response ? [response] : [];
    });
  }
}
