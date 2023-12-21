import { MinimalDTO } from './minimal';
import { ProductDirection, ProductType } from '../enums';

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
  files: MinimalDTO<number, string>[];

  filesMinimal: FileMinimalProduct[];

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface FileMinimalProduct {
  fileId: number;
  fileName: string;
  productFileType: ProductFileType;
}

export type ProductFileType = 'DESIGN_ASM_REPORT' | 'REGULAR';
