import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { RegistrationService } from '../registration.service';

@Component({
  selector: 'app-registration-confirm',
  templateUrl: './confirm.component.html',
})
export class RegistrationConfirmComponent implements OnInit {
  visible = true;
  data: { id: number } | undefined;
  isConfirm: boolean | undefined = undefined;

  private key: string | undefined;

  constructor(private router: Router, private route: ActivatedRoute, private service: RegistrationService) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(data => {
      this.key = data.key;

      this.confirm();
    });
  }

  onClose(): void {
    this.router.navigate(['/login']);
  }

  private confirm(): void {
    if (!this.key) {
      return;
    }

    this.service.confirm(this.key).subscribe(
      data => {
        this.isConfirm = true;
        this.data = data;
      },
      () => {
        this.isConfirm = false;
      }
    );
  }
}
