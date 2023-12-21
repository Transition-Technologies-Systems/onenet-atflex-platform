import { FormControl } from '@angular/forms';

export interface BoundingBox {
  top: number;
  left: number;
  width: number;
  height: number;
}

export interface FileDTO {
  fileName: string;
  fileExtension: string;
  base64StringData: string;
}

export type FormType<ModelDTO> = {
  [Property in keyof ModelDTO]: FormControl<ModelDTO[Property]>;
};

export type Nullable<ModelDTO> = {
  [Property in keyof ModelDTO]: ModelDTO[Property] | null;
};

export interface SplittedMenuItem {
  label: string;
  icon?: string;
  command?: any;
}

export interface DefaultState<T, U = DefaultParameters> {
  totalElements: number;
  data: T[];
  parameters: Partial<U>;
}

export interface DefaultParameters {
  page: number;
  size: number;
  sort: string | string[];

  filters: any;
}
