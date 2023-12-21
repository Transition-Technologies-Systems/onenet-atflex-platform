import { ContentType, LocalizationType } from '@app/shared/enums';
import { DownloadService, HttpService } from '@app/core';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { Observable, Subscription } from 'rxjs';

import { DictionaryLangDto } from '../dictionaries/dictionaries';
import { Injectable } from '@angular/core';
import { SubportfolioDTO } from './subportfolio';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class SubportfoliosService extends HttpService {
  protected url = 'flex-server/api/admin/subportfolio';
  protected derTypeDictUrl = 'flex-server/api/dictionary/get-by-type/DER_TYPE';

  constructor(http: HttpClient) {
    super(http);
  }

  getLocalizationsDict(type: LocalizationType): Observable<DictionaryLangDto[]> {
    return this.get('flex-server/api/admin/localization-types/get-by-type', {
      params: {
        types: [type],
      },
    });
  }

  getSubportfolio(id: number): Observable<SubportfolioDTO> {
    return this.get(`${this.url}/${id}`);
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`${this.url}/files/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  exportXLSX(parameters: DefaultParameters | undefined, allData: boolean): Subscription {
    const type = allData ? 'all' : 'displayed-data';
    const { filters, ...restParameters } = parameters || {};

    return this.get<FileDTO>(`${this.url}/export/${type}`, {
      params: Object.assign(allData ? {} : filters, {
        ...restParameters,
      }),
    }).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<SubportfolioDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<SubportfolioDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: SubportfolioDTO, files: File[]): Observable<void> {
    return this.post(`${this.url}/create`, this.formatData(data, files));
  }

  update(id: number, data: SubportfolioDTO, files: File[]): Observable<void> {
    return this.post(`${this.url}/update`, this.formatData(data, files));
  }

  private formatData(data: SubportfolioDTO, files: File[]): FormData {
    const { ...form } = data;
    const formData = new FormData();

    const subportfolio = this.formatDateTime(form, ['validFrom', 'validTo']);

    formData.append('subportfolioDTO', new Blob([JSON.stringify(subportfolio)], { type: 'application/json' }));

    if (files.length) {
      files.forEach((regularFile: File) => {
        formData.append('files', regularFile);
      });
    }

    return formData;
  }
}
