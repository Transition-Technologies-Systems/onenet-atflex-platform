export interface ProvideDialogDTO {
  title: string;
  description: string;
  filename: string | null;
  file: any;
  dataOfferingId: string;
  code: string;
}

export interface ProvideDialogDictItemDTO {
  id: number;
  onenetId: string;
  title: string;
  serviceCode: string;
  name: string;
}
