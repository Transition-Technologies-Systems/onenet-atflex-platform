import { ContentType, DerType, LocalizationType, Role } from '@app/shared/enums';
import { DerTypeDTO, DerTypeMinDTO, Tab, UnitDTO } from './unit';
import { DefaultParameters, Dictionary, FileDTO, Pageable } from '@app/shared/models';
import { DownloadService, HttpService } from '@app/core';
import { Observable, Subscription, map, tap } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LocalizationTypeDTO } from '../dictionaries/dictionaries';
import { SchedulingUnitProposalDTO } from './invite-der/invite-der';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class UnitsService extends HttpService {
  protected url = 'flex-server/api/admin/units';
  protected derTypeDictUrl = 'flex-server/api/dictionary/get-by-type/DER_TYPE';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  getUnit(id: number): Observable<UnitDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getCompanies(active?: boolean | null): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        active,
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getDerType(row: UnitDTO): string {
    const energyStorageType = row.derTypeEnergyStorage;
    const generationType = row.derTypeGeneration;
    const receptionType = row.derTypeReception;

    const getTypeName = (data: DerTypeMinDTO): string => {
      const subType = this.translate.instant(data.nlsCode);
      const type = this.translate.instant(`DerType.${data.type}`);

      return `${type}: ${subType}`;
    };

    const data = [
      energyStorageType ? getTypeName(energyStorageType) : undefined,
      generationType ? getTypeName(generationType) : undefined,
      receptionType ? getTypeName(receptionType) : undefined,
    ];

    return data.filter(Boolean).join('/ ');
  }

  getDerTypesDict(): Observable<DerTypeDTO[]> {
    return this.get(`${this.derTypeDictUrl}`);
  }

  getDerTypesWithType(): Observable<Array<DerTypeDTO & { type: DerType }>> {
    return this.get('flex-server/api/dictionary/get-der-types');
  }

  getLocalizationsDict(): Observable<LocalizationTypeDTO[]> {
    return this.get('flex-server/api/admin/localization-types/get-by-type', {
      params: {
        types: [
          LocalizationType.COUPLING_POINT_ID,
          LocalizationType.POWER_STATION_ML_LV_NUMBER,
          LocalizationType.POINT_OF_CONNECTION_WITH_LV,
        ],
      },
    });
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('units.tabs.list'),
        type: 'list',
      },
      {
        label: this.translate.instant('units.tabs.selfSchedules'),
        type: 'self-schedules',
      },
    ];
  }

  getUnits(): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; sder: boolean }[]>(`flex-server/api/admin/units/get-all`, {
      params: {
        'certified.equals': true,
      },
    }).pipe(
      map(response =>
        response.map(({ id, name, sder, ...rest }) => ({
          id,
          name,
          sder,
          ...rest,
          value: id,
          label: `${name}${sder ? '(SDER)' : ''}`,
        }))
      )
    );
  }

  exportXLSX(parameters: DefaultParameters | undefined, allData: boolean): Subscription {
    const type = allData ? 'all' : 'displayed-data';
    const { filters, ...restParameters } = parameters || {};

    return this.get<FileDTO>(`${this.url}/export/${type}`, {
      params: Object.assign(allData ? {} : filters, {
        ...restParameters,
      }),
    })
      .pipe(
        tap(({ fileName, base64StringData }: FileDTO) =>
          DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
        )
      )
      .subscribe();
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<UnitDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<UnitDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: UnitDTO): Observable<void> {
    return this.post(`${this.url}`, this.formatDateTime(data, ['validFrom', 'validTo']));
  }

  saveProposal(data: Partial<SchedulingUnitProposalDTO>): Observable<void> {
    return this.post('flex-server/api/admin/scheduling-units/proposal/invite-der', data);
  }

  update(id: number, data: UnitDTO): Observable<void> {
    return this.put(`${this.url}`, this.formatDateTime(data, ['validFrom', 'validTo']));
  }
}
