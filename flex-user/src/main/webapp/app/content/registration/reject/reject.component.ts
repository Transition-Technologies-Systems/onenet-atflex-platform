import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AppToastrService } from '@app/core';

import { RegistrationService } from '../registration.service';

@Component({
  selector: 'app-registration-reject',
  templateUrl: './reject.component.html',
})
export class RegistrationRejectComponent implements OnInit {
  visible = true;

  private key: string | undefined;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private toastr: AppToastrService,
    private service: RegistrationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(data => {
      this.key = data.key;
    });
  }

  onClose(): void {
    this.router.navigate(['/login']);
  }

  confirmOption(): void {
    if (!this.key) {
      return;
    }

    this.service.withdraw(this.key, true).subscribe(
      () => {
        this.toastr.info('registration.reject.confirmOption.success', undefined, { life: 5000 });
        this.router.navigate(['/login']);
      },
      () => {
        this.toastr.info('registration.reject.confirmOption.error', undefined, { life: 5000 });
      }
    );
  }

  rejectOption(): void {
    if (!this.key) {
      return;
    }

    this.service.withdraw(this.key, false).subscribe(
      () => {
        this.toastr.info('registration.reject.rejectOption.success', undefined, { life: 5000 });
        this.router.navigate(['/login']);
      },
      () => {
        this.toastr.info('registration.reject.rejectOption.error', undefined, { life: 5000 });
      }
    );
  }
}
