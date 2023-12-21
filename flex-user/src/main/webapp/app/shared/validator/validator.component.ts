import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-validator',
  templateUrl: './validator.component.html',
})
export class ValidatorComponent {
  @Input() message: string | undefined;
  @Input() parameters: object = {};
}
