import { Role, VolumeUnit } from '../enums';
import { SchedulingUnitDTO, UnitMinDTO } from '@app/content/scheduling-units/scheduling-units';

import { Dictionary } from './dictionary';
import { FspDTO } from './fsps';
import { ProductDTO } from './product';
import { UnitDTO } from '@app/content/units/unit';

export interface FlexPotentialDTO {
  id: number;
  productId: number;
  fspId: number;
  unitId: number;
  unitIds: number[];
  volume: number;
  volumeUnit: string;
  divisibility: boolean;
  validFrom: string;
  validTo: string;
  active: boolean;
  productPrequalification: boolean;
  staticGridPrequalification: boolean;
  fsp: FspDTO;
  product: ProductDTO;
  units: UnitDTO[];
  schedulingUnit: SchedulingUnitDTO;
  filesMinimal: FileMinimal[];
  fullActivationTime: number;
  minDeliveryDuration: number;
  createdBy: string;
  createdDate: string;
  createdByRole: Role;
  lastModifiedBy: string;
  lastModifiedByRole: Role;
  lastModifiedDate: string;
  version: number;
}

export interface FileMinimal {
  fileId: number;
  fileName: string;
}

export interface FlexPotentialMinimalDTO {
  id: number;
  fsp: FspDTO;

  ders?: UnitMinDTO[];
  volumeUnit?: VolumeUnit;
  schedulingUnitType?: Dictionary;

  createdBy: string;
  createdDate: string;
  createdByRole: Role;
  lastModifiedBy: string;
  lastModifiedDate: string;
}
