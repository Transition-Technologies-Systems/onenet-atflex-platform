import { ChatMessageDatePipe, ChatMessagesContainerComponent } from './chat-messages-container';
import { DictionaryNamePipe, FilterPipe, FormatVolumePipe, SearchSelectPipe, PrintDictionaryPipe } from './pipe';
import { ViewConfigurationResolver, ViewConfigurationService } from './view-configuration';

import { AppCalendarLibrary } from './calendar';
import { CalendarComponent } from './calendar/calendar.component';
import { ColumnsConfigurationComponent } from './columns-configuration/columns-configuration.component';
import { ConfirmModalComponent } from './confirm-modal/confirm-modal.component';
import { DialogService } from 'primeng/dynamicdialog';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { HasAnyAuthorityDirective } from './directive/has-any-authority.directive';
import { HasAnyRoleDirective } from './directive/has-any-role.directive';
import { HasAuthorityDirective } from './directive/has-authority.directive';
import { HasRoleDirective } from './directive/has-role.directive';
import { InputNumberComponent } from './input-number/input-number.component';
import { LanguageChangeComponent } from './language-change/language-change.component';
import { ModalComponent } from './modal/modal.component';
import { ModalService } from './modal/modal.service';
import { MultiselectComponent } from './multiselect/multiselect.component';
import { NgModule } from '@angular/core';
import { NumeralPipe } from './pipe/numeral.pipe';
import { PaginationComponent } from './pagination/pagination.component';
import { PreventSpecialCharactersDirective } from './directive/prevent-special-characters.directive';
import { SelectComponent } from './select/select.component';
import { SharedLibraryModule } from '../shared-library.module';
import { SidebarComponent } from './sidebar/sidebar.component';
import { SidebarService } from './sidebar/sidebar.service';
import { StickyTableDirective } from './sticky-table/sticky-table.directive';
import { ToogleDataComponent } from './toogle-data/toogle-data.component';
import { TooltipComponent } from './tooltip/tooltip.component';
import { VersionChooseComponent } from './version-choose/version-choose.component';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { CustomConfirmComponent } from './custom-confirm/custom-confirm.component';
import { PreventDoubleClickDirective } from './directive/prevent-double-click';
import { InputNumberContenteditableComponent } from './input-number-contenteditable/input-number-contenteditable.component';
import { ConfirmModalService } from './confirm-modal/confirm-modal.service';

/**
 * Shared commons - universal components
 */
@NgModule({
  imports: [SharedLibraryModule, ConfirmDialogModule],
  declarations: [
    AppCalendarLibrary,
    CalendarComponent,
    ChatMessagesContainerComponent,
    ChatMessageDatePipe,
    ConfirmModalComponent,
    ColumnsConfigurationComponent,
    DictionaryNamePipe,
    FileUploadComponent,
    FilterPipe,
    InputNumberComponent,
    InputNumberContenteditableComponent,
    HasAnyAuthorityDirective,
    HasAnyRoleDirective,
    HasAuthorityDirective,
    HasRoleDirective,
    ModalComponent,
    MultiselectComponent,
    NumeralPipe,
    SelectComponent,
    PaginationComponent,
    SidebarComponent,
    StickyTableDirective,
    ToogleDataComponent,
    TooltipComponent,
    LanguageChangeComponent,
    VersionChooseComponent,
    PreventSpecialCharactersDirective,
    CustomConfirmComponent,
    PreventDoubleClickDirective,
    FormatVolumePipe,
    SearchSelectPipe,
    PrintDictionaryPipe,
  ],
  exports: [
    AppCalendarLibrary,
    CalendarComponent,
    ChatMessagesContainerComponent,
    ChatMessageDatePipe,
    ConfirmModalComponent,
    ColumnsConfigurationComponent,
    DictionaryNamePipe,
    FileUploadComponent,
    FilterPipe,
    InputNumberComponent,
    InputNumberContenteditableComponent,
    HasAnyAuthorityDirective,
    HasAnyRoleDirective,
    HasAuthorityDirective,
    HasRoleDirective,
    ModalComponent,
    MultiselectComponent,
    NumeralPipe,
    SelectComponent,
    PaginationComponent,
    SidebarComponent,
    StickyTableDirective,
    ToogleDataComponent,
    TooltipComponent,
    LanguageChangeComponent,
    VersionChooseComponent,
    PreventSpecialCharactersDirective,
    CustomConfirmComponent,
    PreventDoubleClickDirective,
    FormatVolumePipe,
    SearchSelectPipe,
    PrintDictionaryPipe,
  ],
  providers: [
    ConfirmModalService,
    DialogService,
    ModalService,
    SidebarService,
    ViewConfigurationResolver,
    ViewConfigurationService,
    ConfirmationService,
    CustomConfirmComponent,
  ],
})
export class SharedCommonsModule {}
