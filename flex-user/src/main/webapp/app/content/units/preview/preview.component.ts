import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AuthService } from '@app/core';
import { Dictionary } from '@app/shared/models';
import { ModalService } from '@app/shared/commons';
import { UnitDTO } from '../unit';
import { UnitsInviteDerComponent } from '../invite-der';
import { UnitsService } from '../units.service';

@Component({
  selector: 'app-units-preview',
  templateUrl: './preview.component.html',
})
export class UnitsPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: UnitDTO = this.config?.data;

  hasRole = false;
  canAddToBsp = false;

  constructor(
    public ref: DynamicDialogRef,
    private service: UnitsService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    public config: DynamicDialogConfig,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    this.getData();
    this.checkRole();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  getDerType(row: UnitDTO): string {
    return this.service.getDerType(row);
  }

  invite(): void {
    if (!this.data.certified) {
      return;
    }

    this.modalService.open(UnitsInviteDerComponent, {
      data: {
        id: this.versionId,
      },
    });
  }

  private checkRole(): void {
    this.authService.hasRole('ROLE_BALANCING_SERVICE_PROVIDER').then((hasRole: boolean) => {
      this.service.getScheduleUnits(this.data.id).subscribe((response: Dictionary[]) => {
        this.hasRole = hasRole && !!response.length;

        if (this.hasRole) {
          this.service.canDerBeAddedToBspSchedulingUnit(this.config.data?.id).subscribe((canAddToBsp: boolean) => {
            this.canAddToBsp = canAddToBsp;
          });
        }

        this.cdr.markForCheck();
      });
    });
  }

  private getData(): void {
    this.service.getUnit(this.versionId).subscribe((response: UnitDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
