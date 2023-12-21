export interface FspUserRegistrationCommentDTO {
  id: number;
  text: string;
  fspUserRegistrationId: number;

  userId: number;
  createdBy: string;
  createdDate: string;

  creationSource: FspUserRegistrationCommentStatus;
  files: { value: string; id: number }[];
}

export enum FspUserRegistrationCommentStatus {
  INITIAL = 'INITIAL',
  GENERATED = 'GENERATED',
  ADDED = 'ADDED',
}
