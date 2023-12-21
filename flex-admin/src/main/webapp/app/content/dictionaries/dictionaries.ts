import { DerType, LocalizationType } from '@app/shared/enums';

import { ProductDTO } from '@app/shared/models';

export interface DictionaryLangDto {
  id: number;
  descriptionEn?: string;
  descriptionPl?: string;
  createdDate?: string;
  lastModifiedDate?: string;
  lvModel?: boolean;
  areaName?: string;

  sderPoint?: number;
  products?: ProductDTO[];

  name?: string;
  type?: LocalizationType | DerType;
}

export interface LocalizationTypeDTO {
  name: string;
  id: number;
  type: LocalizationType;
}

export interface KdmTimestampModelDTO {
  id?: number | null;
  timestamp: string;
  fileName: string | null;
  fileDTO: any;
  kdmModelId: number;
}

export interface SimpleKdmTimestampModelForImport {
  timestamp: string;
  kdmModelId: number;
}

export interface KdmVerifyBody {
  file: File;
  kdmFileId?: number;
  timestamp: string;
  kdmModelId: number;
}
