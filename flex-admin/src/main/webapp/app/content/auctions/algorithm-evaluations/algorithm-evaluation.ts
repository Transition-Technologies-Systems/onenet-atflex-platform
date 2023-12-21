import { AuctionOfferStatus } from '../enums';

export interface AlgorithmEvaluationDTO {
  evaluationId: number;
  typeOfAlgorithm: AlgorithmType;
  deliveryDate: string;
  creationDate: string;
  endDate: string;
  offers: {
    companyName: string;
    potentialId: number;
    potentialName: string;
    id: number;
    price: string;
    status: AuctionOfferStatus;
    volume: string;
  }[];
  status: AlgorithmStatus;
}

export interface AlgorithmEvaluationLogDTO {
  fileName: string;
  content: string[];
}

export enum AlgorithmStatus {
  EVALUATING = 'EVALUATING',
  COMPLETED = 'COMPLETED',
  FAILURE = 'FAILURE',
}

export enum AlgorithmType {
  PBCM = 'PBCM',
  BM = 'BM',
  DANO = 'DANO',
  DISAGGREGATION = 'DISAGGREGATION',
}
