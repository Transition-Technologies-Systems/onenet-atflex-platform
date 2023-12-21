import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivationsSettlementsComponent } from './activations-settlements.component';
import { ActivationsSettlementsService } from './activations-settlements.service';
import { SharedModule } from '@app/shared';
import { ActivationsSettlementsRoutingModule } from './activations-settlements.routing';
import { ActivationsSettlementsDialogComponent } from './dialog/dialog.component';
import { ActivationsSettlementsDialogService } from './dialog/dialog.service';
import { ActivationsSettlementsPreviewComponent } from './preview/preview.component';
import { ActivationsSettlementsExportComponent } from './export/export.component';

@NgModule({
  declarations: [
    ActivationsSettlementsComponent,
    ActivationsSettlementsDialogComponent,
    ActivationsSettlementsPreviewComponent,
    ActivationsSettlementsExportComponent,
  ],
  imports: [CommonModule, SharedModule, ActivationsSettlementsRoutingModule],
  providers: [ActivationsSettlementsService, ActivationsSettlementsDialogService],
})
export class ActivationsSettlementsModule {}
