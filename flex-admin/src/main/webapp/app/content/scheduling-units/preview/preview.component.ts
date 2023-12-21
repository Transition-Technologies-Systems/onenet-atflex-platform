import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SchedulingUnitDTO, UnitMinDTO } from '../scheduling-units';
import { Subject, takeUntil, zip } from 'rxjs';

import { AuthService } from '@app/core';
import { ModalService } from '@app/shared/commons';
import { ProposalComponent } from '@app/shared/proposal';
import { SchedulingUnitsService } from '../scheduling-units.service';
import { UnitDTO } from '@app/content/units/unit';

@Component({
  selector: 'app-scheduling-units-preview',
  templateUrl: './preview.component.html',
})
export class SchedulingUnitsPreviewComponent implements OnInit, OnDestroy {
  hasRole = false;
  isRegister = false;
  refreshAfterClose = false;
  fspWithDers: string[] = [];
  dersData: { [id: number]: UnitDTO } = {};
  ders: Map<string, UnitMinDTO[]> = new Map();
  versionId: number = this.config?.data.id;
  data: SchedulingUnitDTO = this.config?.data;

  private destroy$ = new Subject<void>();

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    public config: DynamicDialogConfig,
    private modalService: ModalService,
    private service: SchedulingUnitsService
  ) {
    this.isRegister = config?.data?.isRegister;
  }

  ngOnInit(): void {
    this.getData();
    this.checkRole();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  proposal(): void {
    this.refreshAfterClose = true;

    this.modalService.open(ProposalComponent, {
      data: {
        schedulingUnit: this.data,
      },
    });
  }

  private getData(): void {
    zip(this.service.getSchedulingUnits(this.versionId), this.service.getSchedulingDers(this.versionId))
      .pipe(takeUntil(this.destroy$))
      .subscribe(([response, ders]: [SchedulingUnitDTO, Map<string, UnitMinDTO[]>]) => {
        this.data = response;

        this.fspWithDers = Object.keys(ders);
        this.ders = new Map(Object.entries(ders));

        const unitIds = Object.entries(ders).reduce((currentData: number[], [, dersData]: [string, UnitMinDTO[]]) => {
          const ids = dersData.map(({ id }) => id);

          return [...currentData, ...ids];
        }, []);

        this.getUnit(unitIds);

        this.cdr.markForCheck();
      });
  }

  private getUnit(ids: number[]): void {
    const obs = ids.map((id: number) => this.service.getUnit(id));

    zip(...obs)
      .pipe(takeUntil(this.destroy$))
      .subscribe((response: UnitDTO[]) => {
        this.dersData = response.reduce((current: { [id: number]: UnitDTO }, der: UnitDTO) => {
          return {
            ...current,
            [der.id]: der,
          };
        }, {});
      });
  }

  private checkRole(): void {
    this.authService.hasRole('ROLE_ADMIN').then((hasRole: boolean) => {
      this.hasRole = hasRole;
      this.cdr.markForCheck();
    });
  }
}
