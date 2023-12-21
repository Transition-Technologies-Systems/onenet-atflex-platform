import * as fileSaver from 'file-saver';

import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';

import { FileUpload } from 'primeng/fileupload';
import { Helpers } from '../helpers';

export interface FileData {
  fileId: number;
  fileName: string;
}

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FileUploadComponent {
  @Input() files: Array<FileData | File> = [];
  @Input() maxFileSize = 10000000;
  @Input() multiple = true;
  @Input() required = false;
  @Input() disabled = false;
  @Input() customError: string | undefined;
  @Input() fileLimit: number | null = null;
  @Input() acceptFormats = '.doc, .docx, .pdf, .txt, .xls, .xlsx';

  @Input()
  set formTouched(touched: boolean) {
    this.touched = touched || this.touched;
  }

  @ViewChild(FileUpload) fileUpload: FileUpload | undefined;

  @Output() download = new EventEmitter<number>();
  @Output() removeFileChange = new EventEmitter<number[]>();
  @Output() fileSelected = new EventEmitter<Array<File>>();

  selectedFiles: File[] = [];
  removeId: number[] = [];

  touched = false;

  get invalid(): boolean {
    return this.required && this.totalFiles.length === 0;
  }

  get totalFiles(): Array<FileData | File> {
    return [...this.files, ...this.selectedFiles].filter((file: FileData | File) => {
      if (file instanceof File) {
        return true;
      }

      return !this.removeId.includes(file.fileId);
    });
  }

  formatSize = Helpers.formatSize;

  private clicked = false;

  constructor() {}

  downloadFile(file: FileData | File): void {
    if (file instanceof File) {
      fileSaver.saveAs(file, file.name);
    } else {
      this.download.emit(file.fileId);
    }
  }

  getFileName(file: FileData | File): string {
    if (file instanceof File) {
      return file.name;
    }

    return file.fileName;
  }

  onFileSelect({ currentFiles }: any): void {
    this.selectedFiles = currentFiles;

    this.fileSelected.emit(this.selectedFiles);
  }

  onMouseleave(): void {
    if (this.clicked) {
      this.touched = true;
    }
  }

  onTouched(): void {
    this.clicked = true;
  }

  removeFile(event: Event, index: number): void {
    const file = this.totalFiles[index];

    if (!(file instanceof File)) {
      this.removeId = [...this.removeId, file.fileId];
      this.removeFileChange.emit(this.removeId);
    } else {
      const fileIndex = index - this.files.length;
      this.selectedFiles.splice(fileIndex, 1);
      this.fileUpload?.remove(event, fileIndex);
    }

    this.fileSelected.emit(this.selectedFiles);
  }
}
