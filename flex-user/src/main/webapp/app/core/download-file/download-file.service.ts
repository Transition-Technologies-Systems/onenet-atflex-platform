import * as fileSaver from 'file-saver';

import { HttpResponse } from '@angular/common/http';
import { ContentType } from '@app/shared/enums';

import { FileExtension } from './file-extensions';

export class DownloadService {
  /**
   * Save ArrayBuffer file
   *
   * @param response The http response with file bytes
   * @param extension The file extensions, default XLSX
   */
  static saveFile(response: HttpResponse<string>, extension: FileExtension = FileExtension.XLSX): void {
    const blob: Blob = this.arrayBufferToBlob(response.body, response.headers.get('Content-Type'));
    const fileName = this.getFileName(response, extension);

    fileSaver.saveAs(blob, fileName);
  }

  /**
   * Save file with parameters
   *
   * @param body The file body
   * @param fileName The file name
   * @param contentType The file content type
   */
  static saveFileWithParam(body: string, fileName: string, contentType: ContentType, decode: boolean = false): void {
    const blob: Blob = decode ? this.arrayBufferToBlob(body, contentType) : new Blob([body], { type: contentType });

    fileSaver.saveAs(blob, fileName);
  }

  /**
   * Get filename from Content-Disposition headers
   */
  static getFileName(response: HttpResponse<any>, defaultExtension: FileExtension, defaultName: string = 'file'): string {
    const contentDisposition = response.headers.get('Content-Disposition');
    const [, filename = null] = contentDisposition ? contentDisposition.match(/.*filename="?([^;"]+)"?.*/) || [] : [];

    if (!filename) {
      return `${defaultName}.${defaultExtension.toLowerCase()}`;
    }

    return decodeURIComponent(filename).replace(/[<>:"\\|?*]+/g, '_');
  }

  /**
   * Convert ArrayBuffer to Blob
   *
   * @param content file bytes
   * @param type file type
   */
  static arrayBufferToBlob(content: string | null, type: string | null): Blob {
    try {
      const fileByte = atob(content || '');
      const fileByteNumbers = new Array(fileByte.length);

      for (let i = 0; i < fileByte.length; i++) {
        fileByteNumbers[i] = fileByte.charCodeAt(i);
      }

      return new Blob([new Uint8Array(fileByteNumbers)], { type: type || ContentType.TXT });
    } catch (e) {
      return new Blob([content || ''], { type: type || ContentType.TXT });
    }
  }
}
