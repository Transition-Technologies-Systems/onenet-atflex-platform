import { DefaultParameters, Dictionary, FileDTO, LocalizationTypeDTO, Pageable, ProductDTO } from '@app/shared/models';
import { DownloadService, HttpService} from '@app/core';
import { Observable, Subscription, map, of, tap } from 'rxjs';
import { SchedulingUnitDTO, Tab, UnitMinDTO } from './scheduling-units';

import { ContentType } from '@app/shared/enums';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { UnitDTO } from '../units/unit';

@Injectable()
export class SchedulingUnitsService extends HttpService {
  protected url = 'api/user/scheduling-units';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`${this.url}/files/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  exportXLSX(parameters: DefaultParameters | undefined, allData: boolean, isRegister: boolean): Subscription {
    const type = allData ? 'all' : 'displayed-data';
    const { filters, ...restParameters } = parameters || {};
    const url = isRegister ? `${this.url}/register/export` : `${this.url}/export`;

    return this.get<FileDTO>(`${url}/${type}`, {
      params: Object.assign(allData ? {} : filters, restParameters),
    })
      .pipe(
        tap(({ fileName, base64StringData }: FileDTO) =>
          DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
        )
      )
      .subscribe();
  }

  getSchedulungUnitTypes(): Observable<Dictionary[]> {
    return this.get<Array<Dictionary & { products: ProductDTO[] }>>(`api/user/su-types/minimal`).pipe(
      map(response =>
        response.map(({ products, ...rest }) => ({
          ...rest,
          prompt: products?.map(({ shortName }) => shortName).join(', '),
        }))
      )
    );
  }

  getSchedulingDers(id: number): Observable<Map<string, UnitMinDTO[]>> {
    return this.get(`${this.url}/${id}/ders`);
  }

  getSchedulingUnits(id: number): Observable<SchedulingUnitDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getUnit(id: number): Observable<UnitDTO> {
    return this.get(`api/user/units/${id}`);
  }

  getLocalizationsDict(unitIds: number[]): Observable<LocalizationTypeDTO[]> {
    if (!unitIds?.length) {
      return of([]);
    }

    return this.get<LocalizationTypeDTO[]>('api/admin/localization-types/get-by-unit-ids', {
      params: {
        unitIds,
      },
    }).pipe(
      map((response: LocalizationTypeDTO[]) =>
        response.map((value: LocalizationTypeDTO) => ({
          ...value,
          name: `${value.name} (${this.translate.instant(`LocalizationType.${value.type}`)})`,
        }))
      )
    );
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('schedulingUnits.tabs.list'),
        type: 'list',
      },
      {
        label: this.translate.instant('schedulingUnits.tabs.types'),
        type: 'types-su',
      },
    ];
  }

  isCurrentFspJoinedWithOtherBspBySchedulingUnit(bspId?: number): Observable<boolean> {
    return this.get(`${this.url}/proposal/is-current-fsp-joined-with-other-bsp-by-scheduling-unit`, {
      params: { bspId },
    });
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<SchedulingUnitDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<SchedulingUnitDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: SchedulingUnitDTO, files: File[]): Observable<void> {
    return this.post(`${this.url}/create`, this.formatData(data, files));
  }

  update(id: number, data: SchedulingUnitDTO, files: File[], dersToRemove: number[]): Observable<void> {
    return this.post(`${this.url}/update`, this.formatData(data, files, dersToRemove));
  }

  private formatData(data: SchedulingUnitDTO, files: File[], dersToRemove: number[] = []): FormData {
    const { ...form } = data;
    const formData = new FormData();

    const SchedulingUnitData = this.formatDateTime(form, ['validFrom', 'validTo']);

    formData.append('schedulingUnitDTO', new Blob([JSON.stringify(SchedulingUnitData)], { type: 'application/json' }));
    formData.append('dersToRemove', dersToRemove.join(','));

    if (files.length) {
      files.forEach((regularFile: File) => {
        formData.append('files', regularFile);
      });
    }

    return formData;
  }
}
