import { Component } from '@angular/core';

@Component({
  selector: 'app-custom-confirm',
  template: `<p-confirmDialog key="confirm-dialog" icon="pi pi-exclamation-triangle"></p-confirmDialog>`,
})
export class CustomConfirmComponent {
  createItemListMessage(confirmationMessage: string, fileList: any): string {
    const message = confirmationMessage + this.showFileList(fileList);
    return message;
  }

  private showFileList(fileList: string[] | undefined): string {
    const wrapper = document.createElement('ul');
    wrapper.className = 'mt-3';
    wrapper.innerHTML = fileList?.map(item => `<li>${item}`).join('</li>') as string;
    return wrapper.outerHTML;
  }
}
