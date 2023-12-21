import { FileDTO, FspDTO } from '@app/shared/models';

import { UnitMinDTO } from '@app/content/scheduling-units/scheduling-units';

export interface SelfScheduleFileDTO {
  id: number;
  fileDTO: FileDTO;
  selfScheduleDate: string;
  fsp: FspDTO;
  unit: UnitMinDTO;
}

export interface SelfScheduleDetailDTO {
  date: string;
  derName: string;
  volumes: { id: string; value: string }[];
}

export interface SelfScheduleVolumesDTO {
  id: string;
  value: string;
}

export interface AllSelfScheduleVolumesDTO {
  [key: number]: SelfScheduleVolumesDTO[];
}
