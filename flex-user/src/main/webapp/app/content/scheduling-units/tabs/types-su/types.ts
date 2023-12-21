import { ProductDTO } from '@app/shared/models';

export interface SchedulingUnitTypeDTO {
  id: number;
  descriptionEn: string;
  descriptionPl: string;
  createdDate?: string;
  lastModifiedDate?: string;

  products: ProductDTO[];
}
