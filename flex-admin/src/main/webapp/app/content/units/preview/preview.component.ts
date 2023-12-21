import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AuthService } from '@app/core';
import { ModalService } from '@app/shared/commons';
import { Role } from '@app/shared/enums';
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

  get hasSchedulingUnit(): boolean {
    return !!this.data.schedulingUnit;
  }

  get isFspaOwnerWithoutSubportfolio(): boolean {
    if (this.data.fsp.role === Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED) {
      return !this.data.subportfolio;
    }
    return false;
  }

  get hasntBalancedByFlexPotentialProduct(): boolean {
    if (this.data.fsp.role === Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED) {
      return false;
    }

    return !this.data.balancedByFlexPotentialProduct;
  }

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
    this.authService.hasRole('ROLE_ADMIN').then((hasRole: boolean) => {
      this.hasRole = hasRole;
      this.cdr.markForCheck();
    });
  }

  private getData(): void {
    this.service.getUnit(this.versionId).subscribe((response: UnitDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
