import { Injectable } from '@angular/core';

@Injectable()
export class ChatMessageContainerService {
  isFileTypeValid(accept: string, file: File): boolean {
    const acceptableTypes = accept.split(',').map(type => type.trim());
    for (const type of acceptableTypes) {
      const acceptable = this.isWildcard(type)
        ? this.getTypeClass(file.type) === this.getTypeClass(type)
        : file.type == type || this.getFileExtension(file).toLowerCase() === type.toLowerCase();

      if (acceptable) {
        return true;
      }
    }

    return false;
  }

  isImage(file: File): boolean {
    return /^image\//.test(file.type);
  }

  private getTypeClass(fileType: string): string {
    return fileType.substring(0, fileType.indexOf('/'));
  }

  private getFileExtension(file: File): string {
    return '.' + file.name.split('.').pop();
  }

  private isWildcard(fileType: string): boolean {
    return fileType.indexOf('*') !== -1;
  }
}
