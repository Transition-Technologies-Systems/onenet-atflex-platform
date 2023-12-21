import { DerTypeMinDTO } from '@app/content/units/unit';
import { Dictionary } from '../models';

export interface ProposalDTO {
  id: number;
  status: ProposalStatus;
  schedulingUnitId: number;
  unitId: number;
  senderId: number;
  details: UnitProposalDetailsDTO;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface UnitProposalDetailsDTO {
  fspName: string;
  derName: string;
  derType: string;
  derSourcePower: number;
  derConnectionPower: number;

  derTypeReception?: DerTypeMinDTO;
  derTypeEnergyStorage?: DerTypeMinDTO;
  derTypeGeneration?: DerTypeMinDTO;

  bspName: string;
  productName: string;
  schedulingUnitName: string;
  schedulingUnitType: Dictionary;
}

export enum ProposalStatus {
  NEW = 'NEW',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
}

export enum ProposalSubportfolioType {
  NOSUBPORTFOLIO = 'no-subportfolio',
}
