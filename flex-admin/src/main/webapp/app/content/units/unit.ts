import { DerType } from '@app/shared/enums';
import { FspDTO } from '@app/shared/models';
import { LocalizationTypeDTO } from '../dictionaries/dictionaries';
import { SchedulingUnitDTO } from '../scheduling-units/scheduling-units';
import { SubportfolioDTO } from '../subportfolio/subportfolio';

export interface Tab {
  label: string;
  type: TabType;
}

export type TabType = 'list' | 'self-schedules';

export interface UnitDTO {
  id: number;
  name: string;
  code: string;
  location: string;
  sourcePower: number;
  connectionPower: number;
  directionOfDeviation: string;
  derType: DerTypeDTO;
  aggregated: boolean;
  fspId: number;
  validFrom: string;
  validTo: string;
  active: boolean;
  certified: boolean;
  fsp: FspDTO;
  fspUser: FspUserDTO;
  geoLocations: UnitGeoLocationDTO[];
  schedulingUnit: SchedulingUnitDTO | undefined;
  subportfolio: SubportfolioDTO | undefined;
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
  ppe: string;
  mridDso: string;
  mridTso: string;
  sder: boolean;
  version: number;
  balancedByFlexPotentialProduct: boolean;
  couplingPointIdTypes: LocalizationTypeDTO[];
  powerStationTypes: LocalizationTypeDTO[];
  pointOfConnectionWithLvTypes: LocalizationTypeDTO[];

  pmin: number;
  qmin: number;
  qmax: number;

  derTypeReception?: DerTypeMinDTO;
  derTypeEnergyStorage?: DerTypeMinDTO;
  derTypeGeneration?: DerTypeMinDTO;
}

export interface DerTypeDTO {
  id: number;
  value: string;
  nlsCode: string;
}

export interface DerTypeMinDTO extends DerTypeDTO {
  type: DerType;
}

export interface FspUserDTO {
  fspId: number;
  userId: number;
  firstName: string;
  lastName: string;
  companyName: string;
  fspActive: boolean;
}

export interface UnitGeoLocationDTO {
  id: number | null;
  latitude: string | null;
  longitude: string | null;
  mainLocation: boolean;
}
