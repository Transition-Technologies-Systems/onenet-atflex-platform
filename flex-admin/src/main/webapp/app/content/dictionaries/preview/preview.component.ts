import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { DictionariesService } from '../dictionaries.service';
import { DictionaryLangDto } from '../dictionaries';
import { DictionaryType } from '@app/shared/enums';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
})
export class DictPreviewComponent implements OnInit {
  itemId: number = this.config.data?.model?.id;
  data: DictionaryLangDto = this.config.data?.model;

  get dictionaryType(): DictionaryType {
    return this.config.data?.dictionaryType || DictionaryType.DER_TYPE;
  }

  private destroy$ = new Subject<void>();

  constructor(public ref: DynamicDialogRef, private service: DictionariesService, public config: DynamicDialogConfig) {}

  ngOnInit(): void {
    this.service.getPositionDetails(this.itemId).subscribe((response: DictionaryLangDto) => {
      this.data = response;
    });
  }

  close(): void {
    this.ref.close();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getProductValues(row: DictionaryLangDto): string {
    const products = row.products ?? [];

    return products.map(({ shortName }) => shortName).join(', ');
  }
}
