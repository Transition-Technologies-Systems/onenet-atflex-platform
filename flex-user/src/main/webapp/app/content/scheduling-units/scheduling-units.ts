import { Dictionary, FileMinimal, FspDTO } from '@app/shared/models';

export interface SchedulingUnitDTO {
  id: number;

  name: string;
  active: boolean;
  numberOfDers: number;
  numberOfDersProposals: number;
  schedulingUnitType: Dictionary;
  bsp: FspDTO;
  readyForTests: boolean;
  certified: boolean;
  units: UnitMinDTO[];
  couplingPoints: Dictionary[];
  primaryCouplingPoint: Dictionary;

  filesMinimal: FileMinimal[];

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface UnitMinDTO {
  id: number;
  name: string;
  sourcePower?: number;
  fspCompanyName: string;
  createdDate: string;
  sder: boolean;
  pmin?: number;
}

export type TabType = 'list' | 'types-su';

export interface Tab {
  label: string;
  type: TabType;
}
