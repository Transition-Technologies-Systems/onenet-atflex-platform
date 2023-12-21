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

  certificationChangeLockedUntil: string;
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
