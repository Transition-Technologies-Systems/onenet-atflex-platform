import { AlgorithmEvaluationsComponent, AlgorithmEvaluationsService } from './algorithm-evaluations';
import { AuctionBidComponent, AuctionBidModalComponent } from './bid-dialog';
import { AuctionOffersComponent, AuctionOffersService } from './offers';
import { AuctionsCmVcFiltersComponent, AuctionsCmVcFiltersService } from './cm-vc/filters';
import { AuctionsDayAheadFiltersComponent, AuctionsDayAheadFiltersService } from './day-ahead/filters';
import { BidsEvaluationComponent, BidsEvaluationService } from './bids-evaluation';
import { CmVcComponent, CmVcService } from './cm-vc';
import { DayAheadComponent, DayAheadService } from './day-ahead';

import { AccordionModule } from 'primeng/accordion';
import { AlgorithmEvaluationsPreviewComponent } from './algorithm-evaluations/preview';
import { AlgorithmEvaluationsProcessLogsComponent } from './algorithm-evaluations/process-logs';
import { AuctionsComponent } from './auctions.component';
import { AuctionsRoutingModule } from './auctions.routing';
import { AuctionsService } from './auctions.service';
import { BidsEvaluationExportDialogComponent } from './bids-evaluation/export';
import { BidsEvaluationFiltersComponent } from './bids-evaluation/filters';
import { BidsEvaluationRunAlgorithmDialogComponent } from './bids-evaluation/run-algorithm';
import { CmVcDialogComponent } from './cm-vc/dialog';
import { CmVcPreviewComponent } from './cm-vc/preview';
import { DataRangeQuartersComponent } from './data-range-quarters';
import { DayAheadDialogComponent } from './day-ahead/dialog';
import { DayAheadPreviewComponent } from './day-ahead/preview';
import { FlexPotentialsSharedModule } from '../flex-potentials/shared';
import { NgModule } from '@angular/core';
import { ProductsSharedModule } from '../products/shared';
import { SchedulingUnitsSharedModule } from '../scheduling-units/shared';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [
    SharedModule,
    AuctionsRoutingModule,
    AccordionModule,
    FlexPotentialsSharedModule,
    ProductsSharedModule,
    SchedulingUnitsSharedModule,
    SelectButtonModule,
  ],
  declarations: [
    AuctionsComponent,
    AuctionBidComponent,
    AuctionOffersComponent,
    AuctionBidModalComponent,

    AuctionsCmVcFiltersComponent,
    AuctionsDayAheadFiltersComponent,

    AlgorithmEvaluationsComponent,
    AlgorithmEvaluationsPreviewComponent,
    AlgorithmEvaluationsProcessLogsComponent,
    BidsEvaluationComponent,
    BidsEvaluationFiltersComponent,
    BidsEvaluationExportDialogComponent,
    BidsEvaluationRunAlgorithmDialogComponent,

    CmVcComponent,
    CmVcDialogComponent,
    CmVcPreviewComponent,

    DayAheadComponent,
    DayAheadDialogComponent,
    DayAheadPreviewComponent,

    DataRangeQuartersComponent,
  ],
  providers: [
    AuctionOffersService,
    AuctionsService,

    AlgorithmEvaluationsService,

    BidsEvaluationService,

    AuctionsCmVcFiltersService,
    AuctionsDayAheadFiltersService,

    CmVcService,
    DayAheadService,
  ],
})
export class AuctionsModule {}
