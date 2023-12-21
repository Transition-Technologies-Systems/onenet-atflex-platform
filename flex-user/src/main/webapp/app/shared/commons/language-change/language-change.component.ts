import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { LanguageService, LocalStorageService } from '@app/core';

import { LANGUAGE_KEY } from '@app/core/language/effects';
import { Language } from '@app/shared/enums';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-language-change',
  templateUrl: './language-change.component.html',
  styleUrls: ['./language-change.component.scss'],
  providers: [LanguageService],
})
export class LanguageChangeComponent implements OnInit, OnDestroy {
  @Input() dropdown = false;

  options: { label: string; value: Language }[] = [
    { label: 'EN', value: 'en' },
    { label: 'PL', value: 'pl' },
  ];

  selectedOption = 'en';

  private destroy$ = new Subject<void>();

  constructor(private service: LanguageService, private localStorageService: LocalStorageService) {}

  ngOnInit(): void {
    const defaultLang = this.localStorageService.getItem(LANGUAGE_KEY);

    if (!!defaultLang) {
      this.selectedOption = defaultLang;
      this.changeLanguage(defaultLang as Language);
    }

    if (this.dropdown) {
      this.service
        .getCurrentLanguage$()
        .pipe(takeUntil(this.destroy$))
        .subscribe((lang: Language) => {
          this.selectedOption = lang;
        });
    }
  }

  ngOnDestroy(): void {
    if (this.dropdown) {
      this.destroy$.next();
      this.destroy$.complete();
    }
  }

  changeLanguage(key: Language): void {
    this.service.changeUserLanguage(key);
    this.selectedOption = key;
  }

  onLangChange(value: any): void {
    this.changeLanguage(value as Language);
  }
}
