import { catchError } from 'rxjs/operators';
import { AppToastrService, DownloadService } from '@app/core';
import { Role } from '@app/shared/enums';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { Helpers, ModalService } from '@app/shared/commons';

import { Component } from '@angular/core';
import { Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { RegistrationService } from './registration.service';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

export interface Dictionaries {
  roles: Dictionary[];
}

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
})
export class RegistrationComponent {
  formTouched = false;
  form = this.service.createForm();
  selectedCountry = CountryISO.Poland;
  prefferedCountries = [CountryISO.Poland];
  searchCountryField = [SearchCountryField.All];

  uploading = false;
  selectedFiles: any[] = [];
  formatSize = Helpers.formatSize;
  acceptFormats = '.doc, .docx, .pdf, .txt, .xls, .xlsx';

  dictionaries: Dictionaries = {
    roles: Helpers.enumToDictionary(Role, 'Role', (value: string) => {
      return [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_BALANCING_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED].includes(
        value as any
      );
    }),
  };

  constructor(
    private router: Router,
    private toastr: AppToastrService,
    public modalService: ModalService,
    private service: RegistrationService,
    private translateService: TranslateService
  ) {}

  getRules(event: any, type: 'RULES' | 'RODO'): void {
    event.preventDefault();

    this.service.getRules(type).subscribe(response => {
      DownloadService.saveFileWithParam(response.base64StringData, response.fileName, response.fileExtension, true);
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  onFileSelect(value: any): void {
    this.selectedFiles = value;
  }

  register(): void {
    this.formTouched = true;

    if (this.form.invalid) {
      this.toastr.error('registration.invalidForm');
      this.form.markAllAsTouched();
      return;
    }

    if (this.form.invalid) {
      this.toastr.error('invalidForm');
      this.form.markAllAsTouched();
      return;
    }

    const data = this.form.getRawValue();

    this.service
      .register(
        {
          ...data,
          langKey: this.translateService.currentLang,
        },
        this.selectedFiles
      )
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error('registration.error');
        })
      )
      .subscribe(() => {
        this.toastr.success('registration.success');
        this.router.navigate(['/login']);
      });
  }
}
