export type TabType = 'REQUEST' | 'INVITATION';

export interface Tab {
  label: string;
  type: TabType;
}

export enum PartnershipStatus {
  NEW = 'NEW',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  CONNECTED_WITH_OTHER = 'CONNECTED_WITH_OTHER'
}

export interface PartnershipDTO {
  id: number;
  bspId: number;
  bspName: string;
  derId: number;
  derName: string;
  fspId: number;
  fspName: string;
  proposalType: TabType;
  schedulingUnitId: number;
  schedulingUnitName: string;
  status: PartnershipStatus;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}
