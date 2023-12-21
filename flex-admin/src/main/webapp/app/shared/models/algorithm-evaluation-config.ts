import { AlgorithmType, KdmType } from '../enums';

export interface AlgorithmEvaluationConfigDTO {
  algorithmType: AlgorithmType;
  kdmType: KdmType;
  deliveryDate: string;
  offers: number[];
}
