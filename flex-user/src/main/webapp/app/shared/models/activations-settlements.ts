export interface ActivationsSettlementsDTO {
  id: number;
  derName: string;
  offerId: number;
  auctionName: string;
  companyName: string;
  acceptedVolumeTooltipVisible: boolean;
  acceptedVolumeCmvcTooltipVisible: boolean;
  acceptedDeliveryPeriodFrom: string;
  acceptedDeliveryPeriodTo: string;
  acceptedVolume: number;
  activatedVolume?: number;
  settlementAmount: number;
  unit: string;
}

export interface ActivationsSettlementsDialogDTO {
  id: number;
  activatedVolume?: number;
  settlementAmount?: number;
  unit: string;
}
