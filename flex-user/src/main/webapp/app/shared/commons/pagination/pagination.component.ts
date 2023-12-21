import { range } from 'lodash-es';

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent implements OnInit, OnChanges {
  @Input() currentPage = 0;
  @Input() showRowsPerPage = true;
  @Input() size = 25;
  @Input() totalElements: number | null = 0;
  @Output() changePage = new EventEmitter<{ page: number; size: number }>();
  elementsOnThePage = { from: 0, to: 0 };
  pages: number[] = [];
  pagesTotal = 0;
  rowsPerPageOptions: number[] = [10, 25, 50, 100];
  rowsPerPageItems: { label: string; value: number }[] = this.rowsPerPageOptions.map((value: number) => ({ label: String(value), value }));

  trackByFn(index: number): number {
    return index;
  }

  ngOnInit(): void {
    this.pages = this.getPages();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.totalElements) {
      this.totalElements = 0;
    }

    if (changes.currentPage || changes.totalElements || changes.size) {
      this.elementsOnThePage = {
        from: Math.max(this.totalElements ? 0 : -1, this.currentPage * this.size) + 1,
        to: Math.min(this.totalElements, (this.currentPage + 1) * this.size),
      };

      if (this.elementsOnThePage.to === 0) {
        this.elementsOnThePage.from = 0;
      }

      this.pagesTotal = Math.ceil(this.totalElements / this.size);
      this.pages = this.getPages();
    }
  }

  /**
   * On rows per page change
   */
  onRppChange(): void {
    this.changePage.emit({ page: 0, size: this.size });
  }

  /**
   * Change page
   *
   * @param page The selected page
   */

  handleLinkClick(page: number): void {
    if (page < 0 || page >= this.pagesTotal) {
      return;
    }

    if (page !== this.currentPage) {
      this.changePage.emit({ page, size: this.size });
    }
  }

  /**
   * Get pages list
   */
  private getPages(): number[] {
    if (this.pagesTotal < 5) {
      return range(1, this.pagesTotal + 1);
    }

    const end = this.currentPage + 5;

    if (end > this.pagesTotal + 1) {
      return range(this.pagesTotal - 4, this.pagesTotal + 1);
    }

    if (this.currentPage < 3) {
      return range(1, 6);
    }

    return range(this.currentPage - 2, end - 2);
  }
}
