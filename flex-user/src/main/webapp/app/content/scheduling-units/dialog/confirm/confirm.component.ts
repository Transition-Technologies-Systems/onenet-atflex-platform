import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { UnitMinDTO } from '../../scheduling-units';

@Component({
  selector: 'app-scheduling-unit-dialog-confirm',
  templateUrl: './confirm.component.html',
  styleUrls: ['./confirm.component.scss'],
  providers: [],
})
export class SchedulingUnitsConfirmDialogComponent implements OnInit {
  ders: UnitMinDTO[] = [];

  constructor(public ref: DynamicDialogRef, public config: DynamicDialogConfig) {}

  ngOnInit(): void {
    const ders: UnitMinDTO[] = this.config.data?.ders ?? [];
    const dersToRemove: number[] = this.config.data?.dersToRemove ?? [];

    this.ders = ders.filter(({ id }) => dersToRemove.includes(id));
  }

  close(result: boolean = false): void {
    this.ref.close(result);
  }
}
