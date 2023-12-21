export enum AuctionEmailCategory {
  BID = 'BID',
  ALGORITHM_RESULT = 'ALGORITHM_RESULT',
  TSO_EXPORT = 'TSO_EXPORT',
  DSO_SETO_EXPORT = 'DSO_SETO_EXPORT',
}

export interface AuctionEmailDTO {
  notifiedEmailAdress: string;
}
