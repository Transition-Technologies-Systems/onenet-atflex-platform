import { Observable } from 'rxjs';
import { Component } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { Dictionary } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ChatService } from '../chat.service';

interface Dictionaries {
  companies$: Observable<Partial<Dictionary>[]>;
}

@Component({
  selector: 'app-new-chat',
  templateUrl: './new-chat.component.html',
})
export class NewChatComponent {
  form = this.fb.group({
    company: [null, Validators.required],
  });

  dictionaries: Dictionaries = {
    companies$: this.service.getCompanies(),
  };

  constructor(
    private fb: UntypedFormBuilder,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private service: ChatService
  ) {}

  close(): void {
    this.ref.close();
  }

  startNewChat(): void {
    this.ref.close(this.form.controls.company.value);
  }
}
