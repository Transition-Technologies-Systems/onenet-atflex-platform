import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Dictionary, FlexPotentialDTO } from '@app/shared/models';

import { FlexPotentialsService } from '../../flex-potentials.service';

@Component({
  selector: 'app-flex-potentials-preview',
  templateUrl: './preview.component.html',
  providers: [FlexPotentialsService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FlexPotentialPreviewComponent implements OnChanges {
  @Input() flexPotentialData: FlexPotentialDTO | undefined;
  @Input() flexPotentialId = -1;
  @Input() embeddedPreview = false;
  @Input() selected = false;
  @Input() borderLeft = true;

  data: FlexPotentialDTO | undefined = undefined;
  users: Dictionary[] = [];

  constructor(private service: FlexPotentialsService, public cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.flexPotentialData?.currentValue) {
      this.data = this.flexPotentialData;
    }

    if (changes.flexPotentialId?.currentValue) {
      this.service.getFlexPotential(this.flexPotentialId).subscribe((product: FlexPotentialDTO) => {
        this.data = product;
        this.cdr.markForCheck();
      });
    }
  }
}
