import { AuctionBidComponent, AuctionBidModalComponent } from './bid-dialog';
import { AuctionOffersComponent, AuctionOffersService } from './offers';
import { AuctionsCmVcFiltersComponent, AuctionsCmVcFiltersService } from './cm-vc/filters';
import { AuctionsDayAheadFiltersComponent, AuctionsDayAheadFiltersService } from './day-ahead/filters';
import { CmVcComponent, CmVcService } from './cm-vc';
import { DayAheadComponent, DayAheadService } from './day-ahead';

import { AccordionModule } from 'primeng/accordion';
import { AuctionsComponent } from './auctions.component';
import { AuctionsRoutingModule } from './auctions.routing';
import { AuctionsService } from './auctions.service';
import { CmVcDialogComponent } from './cm-vc/dialog';
import { CmVcPreviewComponent } from './cm-vc/preview';
import { DataRangeQuartersComponent } from './data-range-quarters';
import { DayAheadDialogComponent } from './day-ahead/dialog';
import { DayAheadPreviewComponent } from './day-ahead/preview';
import { FlexPotentialsSharedModule } from '../flex-potentials/shared';
import { MyOffersComponent } from './my-offers';
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

    CmVcComponent,
    CmVcDialogComponent,
    CmVcPreviewComponent,

    DayAheadComponent,
    DayAheadDialogComponent,
    DayAheadPreviewComponent,

    DataRangeQuartersComponent,

    MyOffersComponent,
  ],
  providers: [
    AuctionOffersService,
    AuctionsService,

    AuctionsCmVcFiltersService,
    AuctionsDayAheadFiltersService,

    CmVcService,
    DayAheadService,
  ],
})
export class AuctionsModule {}
