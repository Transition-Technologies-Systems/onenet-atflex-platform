export interface Pageable<T> {
  content: T[];
  totalElements: number;
}

export interface PageableParams {
  number: number;
  size: number;
}

export class Page {
  size = 50;
  totalElements = 0;
  totalPages = 0;

  set current(page: number) {
    if (page === null) {
      this.currentPage = 0;
    }

    this.currentPage = page + 1;
  }

  get current(): number {
    return this.currentPage;
  }

  get httpParams(): PageableParams {
    return {
      number: this.currentPage ? this.currentPage - 1 : this.currentPage,
      size: this.size,
    };
  }

  private currentPage = 1;

  constructor(
    attr: { page: number; totalElements: number } = {
      page: 0,
      totalElements: 0,
    },
    size: number = 50
  ) {
    this.size = size;
    this.current = attr.page;
    this.totalElements = attr.totalElements;

    this.totalPages = Math.ceil(this.totalElements / this.size);
  }
}
