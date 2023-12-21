import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subject, of } from 'rxjs';
import { PartnershipStatus, TabType } from '../partnership';

import { AuthService } from '@app/core';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { PartnershipFiltersService } from './filters.service';
import { PartnershipService } from '../partnership.service';
import { Role } from '@app/shared/enums';

interface Dictionaries {
  status: Dictionary[];
  sender: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-partnership-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class PartnershipFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;
  @Input() tabType: TabType = 'INVITATION';

  isFsp = false;
  dictionaries$: Dictionaries = {
    status: Helpers.enumToDictionary(PartnershipStatus, 'partnership.status'),
    sender: of([]),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private authService: AuthService, private service: PartnershipService, private filtersService: PartnershipFiltersService) {}

  filter(): void {
    const { sender, receiver, ...rest } = this.filters.getRawValue();
    const senderKey = this.tabType === 'INVITATION' ? 'bspId' : 'fspId';
    const receiverKey = this.tabType === 'INVITATION' ? 'fspId' : 'bspId';

    const filters = {
      ...rest,
      [senderKey]: sender,
      [receiverKey]: receiver,
    };

    this.filterData.emit(filters);
  }

  ngOnInit(): void {
    this.getCompanies();

    if (this.initFilters) {
      this.filters.patchValue(this.initFilters);
    }
  }

  ngOnChanges(): void {
    this.getCompanies();
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }

  private getCompanies(): void {
    this.authService.hasAnyRoles([Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED]).then((isFsp: boolean) => {
      this.isFsp = isFsp;

      if (isFsp) {
        this.dictionaries$.sender = this.service.getFspSenders(this.tabType);
      } else {
        this.dictionaries$.sender = this.service.getBspSenders(this.tabType);
      }
    });
  }
}
