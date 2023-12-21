export interface SchedulingUnitProposalDTO {
  id: number;
  status: SchedulingUnitProposalStatus;
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
}

export enum SchedulingUnitProposalStatus {
  NEW = 'NEW',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
}
