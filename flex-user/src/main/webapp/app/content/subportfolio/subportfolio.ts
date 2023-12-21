import { FileMinimal, FspDTO, LocalizationTypeDTO } from '@app/shared/models';

import { UnitDTO } from '../units/unit';

export interface SubportfolioDTO {
  id: number;
  name: string;
  code: string;
  numberOfDers: string;
  combinedPowerOfDers: number;
  fspId: number;
  validFrom: string;
  validTo: string;
  active: boolean;
  certified: boolean;
  fspa: FspDTO;
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
  mrid: string;
  units: UnitDTO[];
  unitIds: number[];
  couplingPointIdTypes: LocalizationTypeDTO[];

  filesMinimal: FileMinimal[];
}
