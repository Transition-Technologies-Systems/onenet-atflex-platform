import { AfterViewInit, Directive, ElementRef, Input, OnDestroy } from '@angular/core';
import { Subject, fromEvent, takeUntil } from 'rxjs';

import { TableService } from 'primeng/table';

@Directive({
  selector: '[appStickyTable]',
})
export class StickyTableDirective implements AfterViewInit, OnDestroy {
  @Input('stickyInModal') inModal = false;

  private tableGridHeaderRef!: HTMLElement;
  private scrollRef!: HTMLElement | null;
  private tableGridRef!: HTMLElement;

  private destroy$ = new Subject<void>();

  constructor(public tableService: TableService, public elementRef: ElementRef) {}

  ngAfterViewInit() {
    this.tableGridRef = this.elementRef.nativeElement.querySelector('.p-datatable-wrapper');
    this.tableGridHeaderRef = this.elementRef.nativeElement.querySelector('.p-datatable-thead');

    if (this.inModal) {
      this.scrollRef = this.tableGridRef.closest('.app-modal-dialog');
    }

    fromEvent(this.scrollRef ?? document, 'scroll').pipe(takeUntil(this.destroy$)).subscribe(() => this.stickyHeaders());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  stickyHeaders(): void {
    const tableOffsetTop = this.tableGridRef ? this.tableGridRef.getBoundingClientRect().top : 0;

    if (this.tableGridHeaderRef && this.tableGridRef) {
      if (tableOffsetTop < 0) {
        const translate = 'translate(0,' + Math.abs(tableOffsetTop) + 'px)';
        this.tableGridHeaderRef.setAttribute('style', 'transform:' + translate);

        this.tableGridHeaderRef.classList.add('sticky-header');
      } else {
        const translate = 'translate(0, 0px)';
        this.tableGridHeaderRef.setAttribute('style', 'transform:' + translate);
        this.tableGridHeaderRef.classList.remove('sticky-header');
      }
    }
  }
}
