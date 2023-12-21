import { catchError } from 'rxjs/operators';
import * as AuthActions from '@app/core/auth/actions';
import { Component, OnInit } from '@angular/core';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { Subject, takeUntil, skip } from 'rxjs';
import { AppToastrService } from '@app/core';
import { UserProfileService } from './user-profile.service';
import { EmailNotification, UserProfileDTO } from './user-profile';
import { FormGroup, UntypedFormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Store } from '@ngrx/store';
import { isEqual } from 'lodash-es';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss'],
  providers: [UserProfileService],
})
export class UserProfileComponent implements OnInit {
  form!: UntypedFormGroup;
  changePasswordForm!: UntypedFormGroup;
  selectedCountry = CountryISO.Poland;
  prefferedCountries = [CountryISO.Poland];
  searchCountryField = [SearchCountryField.All];
  profileData: UserProfileDTO = {} as UserProfileDTO;
  formModified: boolean = false;
  destroy$ = new Subject<void>();

  checkboxActions = this.service.getNotificationSettingsConfig();
  notificationsSaveBtnVisible: boolean = false;
  emailNotificationsForm!: UntypedFormGroup;
  initialEmailNotificationValue!: EmailNotification;

  constructor(public toastr: AppToastrService, private service: UserProfileService, private store: Store) {
    this.form = this.service.createForm(this.profileData);
    this.changePasswordForm = this.service.createChangePasswordForm();
    this.emailNotificationsForm = new FormGroup({});
  }

  ngOnInit(): void {
    this.getProfileData();
  }

  private getProfileData() {
    this.service
      .getProfileData()
      .pipe(takeUntil(this.destroy$))
      .subscribe(resp => {
        this.profileData = resp;
        this.form.patchValue(this.profileData);
        this.onUserProfileFormValueChange();
        this.emailNotificationsForm = this.service.createEmailNotificationsForm(this.profileData.roles);
        this.service
          .getUserEmailNotificationsSettings()
          .pipe(
            catchError((): any => {
              this.initialEmailNotificationValue = this.emailNotificationsForm.getRawValue();
            })
          )
          .subscribe((data: EmailNotification | any) => {
            this.initialEmailNotificationValue = data;
            this.emailNotificationsForm.patchValue(this.initialEmailNotificationValue);
          });
      });
  }

  saveChanges() {
    let data = { ...this.profileData, ...this.form.getRawValue() };
    data.phoneNumber = data.phoneNumber?.e164Number;
    this.service
      .updateProfileData(data)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error('userProfile.actions.error');
        })
      )
      .subscribe(() => {
        this.toastr.success('userProfile.actions.success');
        this.store.dispatch(AuthActions.updateUser({ user: data }));
        this.formModified = false;
      });
  }

  saveNewPassword() {
    const passwordData = this.changePasswordForm.getRawValue();
    let dataToSend = { currentPassword: passwordData.currentPassword, newPassword: passwordData.newPassword };
    this.service
      .updateUserPassword(dataToSend)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error('userProfile.actions.changePasswordError');
        })
      )
      .subscribe(() => {
        this.toastr.success('userProfile.actions.changePasswordSuccess');
        this.changePasswordForm.reset();
      });
  }

  onUserProfileFormValueChange() {
    this.form.valueChanges.pipe(skip(1)).subscribe(val => {
      if (
        this.profileData.activated !== val.activated ||
        this.profileData.login !== val.login ||
        this.profileData.phoneNumber.e164Number !== val.phoneNumber ||
        this.profileData.lastName !== val.lastName ||
        this.profileData.firstName !== val.firstName
      ) {
        this.formModified = true;
      }
    });
  }

  checkCheckboxAndHeaderVisibility(roles: string[]) {
    let checkboxAndHeaderVisible = false;
    const { roles: profileDataRoles } = this.profileData;
    if (profileDataRoles && profileDataRoles.length) {
      checkboxAndHeaderVisible = profileDataRoles.some(role => roles.includes(role));
    }
    return checkboxAndHeaderVisible;
  }

  checkboxValuesCheck() {
    const formValue = this.emailNotificationsForm.getRawValue();
    const dataEqual = isEqual(this.initialEmailNotificationValue, formValue);
    this.notificationsSaveBtnVisible = !dataEqual;
  }

  saveEmailNotifications() {
    const formValue = this.emailNotificationsForm.getRawValue();
    this.service.updateUserEmailNotificationSettings(formValue).subscribe(() => {
      this.initialEmailNotificationValue = formValue;
      this.checkboxValuesCheck();
      this.toastr.success('userProfile.actions.updateEmailNotificationsSuccess');
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
