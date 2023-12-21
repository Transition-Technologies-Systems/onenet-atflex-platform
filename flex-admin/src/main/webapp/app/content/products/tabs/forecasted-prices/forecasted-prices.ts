import { FileDTO, ProductDTO } from '@app/shared/models';

export interface ForecastedPricesFileDTO {
  id: number;
  fileDTO: FileDTO;
  forecastedPricesDate: string;
  product: ProductDTO;
}

export interface ForecastedPricesDetailDTO {
  date: string;
  productName: string;
  prices: { id: string; value: string }[];
}
