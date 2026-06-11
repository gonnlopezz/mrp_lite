import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [CommonModule],
  templateUrl: './pagination.html',
  styles: ``,
})
export class PaginationComponent { 
  @Input() totalPages: number = 0;
  @Input() last: boolean = false;
  @Input() currentPage: number = 1;
  @Input() number: number = 1;
  @Output() pageChangeRequested = new EventEmitter<number>();
  pages: number[] = [];

  visiblePages: number[] = [];

  constructor() {}

  ngOnChanges(changes: SimpleChanges) {
    this.pages = Array.from(Array(this.totalPages).keys());
    this.updateVisiblePages();
  }

  updateVisiblePages(): void {
    const C = this.number || 0;
    const N = this.totalPages;

    let start = C - 2;
    if (start < 0) {
      start = 0;
    }
    if (start > N - 5) {
      start = N - 5;
    }
    if (start < 0) {
      start = 0;
    }

    this.visiblePages = [];
    for (let i = 0; i < 5; i++) {
      const idx = start + i;
      if (idx >= 0 && idx < N) {
        this.visiblePages.push(idx);
      }
    }
  }

  onPageChange(pageId: number): void {
    if (!this.currentPage) this.currentPage = 1;

    let page = pageId;
    if (pageId === -2) page = 1;

    if (pageId === -1) page = this.currentPage > 1 ? this.currentPage - 1 : 1;

    if (pageId === -3) page = !this.last ? this.currentPage + 1 : this.currentPage;

    if (pageId === -4) page = this.totalPages;

    if (pageId > 1 && this.pages.length >= pageId) page = this.pages[pageId - 1] + 1;

    this.currentPage = page;
    this.pageChangeRequested.emit(page);
  }
}
