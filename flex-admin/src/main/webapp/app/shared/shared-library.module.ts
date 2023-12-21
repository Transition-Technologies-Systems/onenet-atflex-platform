import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AccordionModule } from 'primeng/accordion';
import { BadgeModule } from 'primeng/badge';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { CommonModule } from '@angular/common';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { DynamicDialogModule } from 'primeng/dynamicdialog';
import { FileUploadModule } from 'primeng/fileupload';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MenuModule } from 'primeng/menu';
import { MessageModule } from 'primeng/message';
import { MessagesModule } from 'primeng/messages';
import { MultiSelectModule } from 'primeng/multiselect';
import { NgModule } from '@angular/core';
import { NgxIntlTelInputModule } from 'ngx-intl-tel-input';
import { OverlayModule } from '@angular/cdk/overlay';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { RadioButtonModule } from 'primeng/radiobutton';
import { RouterModule } from '@angular/router';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { TabViewModule } from 'primeng/tabview';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { SplitButtonModule } from 'primeng/splitbutton';

/**
 * Shared library
 */
@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    TabViewModule,
    AccordionModule,
    BadgeModule,
    ButtonModule,
    CheckboxModule,
    ConfirmPopupModule,
    DialogModule,
    DropdownModule,
    DynamicDialogModule,
    FileUploadModule,
    InputNumberModule,
    InputTextModule,
    InputTextareaModule,
    MenuModule,
    MessageModule,
    MessagesModule,
    MultiSelectModule,
    OverlayModule,
    OverlayPanelModule,
    RadioButtonModule,
    ScrollPanelModule,
    TableModule,
    TooltipModule,
    SplitButtonModule,
    NgxIntlTelInputModule,
  ],
  exports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,
    TranslateModule,
    TabViewModule,
    AccordionModule,
    BadgeModule,
    ButtonModule,
    CheckboxModule,
    ConfirmPopupModule,
    DialogModule,
    DropdownModule,
    DynamicDialogModule,
    FileUploadModule,
    InputNumberModule,
    InputTextModule,
    InputTextareaModule,
    MenuModule,
    MessageModule,
    MessagesModule,
    MultiSelectModule,
    OverlayModule,
    OverlayPanelModule,
    RadioButtonModule,
    ScrollPanelModule,
    TableModule,
    TooltipModule,
    SplitButtonModule,
    NgxIntlTelInputModule,
  ],
  providers: [],
})
export class SharedLibraryModule {}
