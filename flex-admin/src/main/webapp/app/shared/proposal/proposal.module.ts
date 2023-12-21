import { NgModule } from '@angular/core';
import { ProposalComponent } from './proposal.component';
import { ProposalConfirmComponent } from './confirm';
import { ProposalService } from './proposal.service';
import { SharedCommonsModule } from '@app/shared/commons/shared-commons.module';
import { SharedLibraryModule } from '@app/shared/shared-library.module';

@NgModule({
  imports: [SharedLibraryModule, SharedCommonsModule],
  declarations: [ProposalComponent, ProposalConfirmComponent],
  providers: [ProposalService],
  exports: [ProposalComponent, ProposalConfirmComponent],
})
export class ProposalModule {}
