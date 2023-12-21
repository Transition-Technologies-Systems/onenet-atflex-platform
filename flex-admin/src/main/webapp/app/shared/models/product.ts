import { ProductDirection, ProductType } from '../enums';

import { MinimalDTO } from './minimal';

export interface ProductDTO {
  id: number;
  fullName: string;
  shortName: string;
  type: ProductType;
  locational: boolean;
  minBidSize: number;
  maxBidSize: number;
  maxFullActivationTime: number;
  minRequiredDeliveryDuration: number;
  hasAsmReport: boolean;
  active: boolean;
  validFrom: string;
  validTo: string;
  psoUserId: number;
  ssoUserIds: number[];
  bidSizeUnit: string;
  direction: ProductDirection;
  balancing: boolean;
  cmvc: boolean;
  files: MinimalDTO<number, string>[];

  filesMinimal: FileMinimalProduct[];

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;

  version: number;
}

export interface FileMinimalProduct {
  fileId: number;
  fileName: string;
}

export type ProductFileType = 'DESIGN_ASM_REPORT' | 'REGULAR';
