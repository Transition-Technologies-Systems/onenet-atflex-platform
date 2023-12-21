export interface FspDTO {
  id: number;
  validFrom: string;
  validTo: string;
  active: boolean;
  ownerId: number;
  role: string;
  companyName: string;
  agreementWithTso: boolean;

  representative: FspRepresentativeDTO;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface FspRepresentativeDTO {
  id: number;
  companyName: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | any;
}
