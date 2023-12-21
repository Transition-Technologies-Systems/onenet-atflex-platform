import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { SchedulingUnitDTO, UnitMinDTO } from '../../scheduling-units';

import { Dictionary } from '@app/shared/models';
import { SchedulingUnitsService } from '../../scheduling-units.service';

@Component({
  selector: 'app-scheduling-units-preview',
  templateUrl: './preview.component.html',
  providers: [SchedulingUnitsService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchedulingUnitPreviewComponent implements OnInit, OnChanges {
  @Input() schedulingUnitData: SchedulingUnitDTO | undefined;
  @Input() schedulingUnitId = -1;
  @Input() embeddedPreview = false;
  @Input() selected = false;
  @Input() borderLeft = true;

  data: SchedulingUnitDTO | undefined = undefined;
  ders: Map<string, UnitMinDTO[]> = new Map();
  fspWithDers: string[] = [];
  users: Dictionary[] = [];

  constructor(private service: SchedulingUnitsService, public cdr: ChangeDetectorRef) {}

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.schedulingUnitData?.currentValue) {
      this.data = this.schedulingUnitData;
      this.getSchedulingDers(this.data?.id);
    }

    if (changes.schedulingUnitId?.currentValue) {
      this.getSchedulingDers();

      this.service.getSchedulingUnits(this.schedulingUnitId).subscribe((product: SchedulingUnitDTO) => {
        this.data = product;
        this.cdr.markForCheck();
      });
    }
  }

  getDersForFsp(fsp: string): UnitMinDTO[] {
    return this.ders.get(fsp) || [];
  }

  private getSchedulingDers(id?: number): void {
    this.service.getSchedulingDers(id || this.schedulingUnitId).subscribe((ders: Map<string, UnitMinDTO[]>) => {
      this.fspWithDers = Object.keys(ders);
      this.ders = new Map(Object.entries(ders));
    });
  }
}
