import { catchError } from 'rxjs';
import * as moment from 'moment';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { Component, OnInit, Optional, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AppToastrService } from '@app/core';

import { ProposalService } from '../proposal.service';
import { NgForm } from '@angular/forms';
import { Dictionary } from '@app/shared/models';
import { DerTypeMinDTO } from '@app/content/units/unit';
import { TranslateService } from '@ngx-translate/core';
import { ProposalDTO, UnitProposalDetailsDTO } from '../proposal';

interface Dictionaries {
  schedulingUnits: Dictionary[];
}

@Component({
  selector: 'app-scheduling-unit-proposal-confirm',
  templateUrl: './confirm.component.html',
})
export class ProposalConfirmComponent implements OnInit {
  @ViewChild('confirmForm') public confirmForm: NgForm | undefined;

  visible = true;
  type: 'BSP' | 'FSP' = 'FSP';
  createdDate: Date = new Date();
  data: ProposalDTO | undefined;
  schedulingUnitId: number | undefined;

  dictionaries: Dictionaries = {
    schedulingUnits: [],
  };

  get notNew(): boolean {
    return !this.data || this.data?.status !== 'NEW';
  }

  private proposalId: number | undefined;

  constructor(
    private router: Router,
    public route: ActivatedRoute,
    public toastr: AppToastrService,
    private service: ProposalService,
    private translate: TranslateService,
    @Optional() public ref: DynamicDialogRef,
    @Optional() public dialogConfig: DynamicDialogConfig
  ) {
    this.type = this.dialogConfig?.data?.type || 'FSP';
  }

  ngOnInit(): void {
    if (this.dialogConfig) {
      this.proposalId = this.dialogConfig.data.id;
      this.getProposalData();
    } else {
      this.route.queryParams.subscribe(data => {
        this.proposalId = data.id;
        this.type = data.type || 'FSP';
        this.getProposalData();
      });
    }
  }

  confirm(): void {
    if (!this.proposalId) {
      return;
    }

    if (this.type === 'FSP' && this.confirmForm?.invalid) {
      this.confirmForm?.controls?.schedulingUnitId?.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    this.service
      .proposalAccept(this.proposalId, this.type, this.schedulingUnitId)
      .pipe(catchError((): any => this.toastr.error('shared.proposal.confirm.accept.error')))
      .subscribe(() => {
        this.toastr.success('shared.proposal.confirm.accept.success');
        this.visible = false;
        this.ref?.close();
      });
  }

  close(): void {
    this.visible = false;
    this.ref?.close();
  }

  getDerType(details: UnitProposalDetailsDTO): string {
    const energyStorageType = details.derTypeEnergyStorage;
    const generationType = details.derTypeGeneration;
    const receptionType = details.derTypeReception;

    const getTypeName = (data: DerTypeMinDTO): string => {
      const subType = this.translate.instant(data.nlsCode);
      const type = this.translate.instant(`DerType.${data.type}`);

      return `${type}: ${subType}`;
    };

    const data = [
      energyStorageType ? getTypeName(energyStorageType) : undefined,
      generationType ? getTypeName(generationType) : undefined,
      receptionType ? getTypeName(receptionType) : undefined,
    ];

    return data.filter(Boolean).join('/ ');
  }

  onClose(): void {
    this.router.navigate(['/scheduling-units']);
  }

  reject(): void {
    if (!this.proposalId) {
      return;
    }

    this.service
      .proposalReject(this.proposalId)
      .pipe(catchError((): any => this.toastr.error('shared.proposal.confirm.reject.error')))
      .subscribe(() => {
        this.toastr.success('shared.proposal.confirm.reject.success');
        this.visible = false;
        this.ref?.close();
      });
  }

  private getProposalData(): void {
    if (!this.proposalId) {
      this.visible = false;
      this.ref?.close();
      return;
    }

    this.service
      .getProposal(this.proposalId)
      .pipe(
        catchError((): any => {
          this.visible = false;
          this.toastr.warning('shared.proposal.invalidKey');

          this.ref?.close();
        })
      )
      .subscribe((proposal: ProposalDTO | any) => {
        this.data = proposal;

        if (this.type === 'FSP') {
          this.getSchedulingUnits();
        }

        this.createdDate = moment(proposal.createdDate).toDate();
      });
  }

  private getSchedulingUnits(): void {
    if (!this.data?.unitId) {
      this.dictionaries.schedulingUnits = [];
      return;
    }

    this.service.getScheduleUnits(this.data?.unitId).subscribe(response => {
      this.dictionaries.schedulingUnits = response;
    });
  }
}
