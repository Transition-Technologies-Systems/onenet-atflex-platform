import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-toogle-data',
  styleUrls: ['./toogle-data.component.scss'],
  templateUrl: './toogle-data.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToogleDataComponent {
  @Input() header = '';
  @Input() selected: boolean | undefined;
}
