import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SchedulingUnitDTO, UnitMinDTO } from '../scheduling-units';

import { SchedulingUnitsService } from '../scheduling-units.service';
import { UnitDTO } from '@app/content/units/unit';
import { zip } from 'rxjs';

@Component({
  selector: 'app-scheduling-units-preview',
  templateUrl: './preview.component.html',
})
export class SchedulingUnitsPreviewComponent implements OnInit {
  isRegister = false;
  refreshAfterClose = false;
  fspWithDers: string[] = [];
  dersData: { [id: number]: UnitDTO } = {};
  ders: Map<string, UnitMinDTO[]> = new Map();
  versionId: number = this.config?.data.id;
  data: SchedulingUnitDTO = this.config?.data;

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private service: SchedulingUnitsService
  ) {
    this.isRegister = config?.data?.isRegister;
  }

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close(this.refreshAfterClose);
  }

  getCouplingPoints(): string {
    return this.data?.couplingPoints?.map(({ name }) => name).join(', ');
  }

  getDersForFsp(fsp: string): UnitMinDTO[] {
    return this.ders.get(fsp) || [];
  }

  getDerData(data: UnitMinDTO, fsp: string): object {
    return { ...data, fsp };
  }

  onDownloadFile(id: number): void {
    this.service.downloadFile(id);
  }

  private getData(): void {
    zip(this.service.getSchedulingUnits(this.versionId), this.service.getSchedulingDers(this.versionId)).subscribe(
      ([response, ders]: [SchedulingUnitDTO, Map<string, UnitMinDTO[]>]) => {
        this.data = response;

        this.fspWithDers = Object.keys(ders);
        this.ders = new Map(Object.entries(ders));

        const unitIds = Object.entries(ders).reduce((currentData: number[], [, dersData]: [string, UnitMinDTO[]]) => {
          const ids = dersData.map(({ id }) => id);

          return [...currentData, ...ids];
        }, []);

        this.getUnit(unitIds);

        this.cdr.markForCheck();
      }
    );
  }

  private getUnit(ids: number[]): void {
    const obs = ids.map((id: number) => this.service.getUnit(id));

    zip(...obs).subscribe((response: UnitDTO[]) => {
      this.dersData = response.reduce((current: { [id: number]: UnitDTO }, der: UnitDTO) => {
        return {
          ...current,
          [der.id]: der,
        };
      }, {});
    });
  }
}
