import { ChangeDetectorRef, Component } from '@angular/core';
import { PaginationComponent } from '../pagination/pagination.component';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { ResultsPage } from '../results-page';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { productService } from './product.service';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from './product';

@Component({
  selector: 'app-products',
  imports: [PaginationComponent, RouterModule, CommonModule, FormsModule],
  templateUrl: './products.html',
  styles: ``
})
export class ProductsComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  openedProducts: Set<number> = new Set();
  searchTerm: string = '';

  constructor(
    private productService: productService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal) { }

  getProducts(): void {
    this.productService.byPage(this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.currentPage = 1;
      this.getProducts();
      return;
    }

    this.currentPage = 1;
    this.productService.search(this.searchTerm, this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  delete(id: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      centered: true,
      backdrop: 'static'
    });

    modalRef.componentInstance.title = 'Eliminar Producto';
    modalRef.componentInstance.message = `¿Estás seguro de eliminar este producto? Esta acción no se puede deshacer.`;
    modalRef.componentInstance.btnOkText = 'Sí, Eliminar';
    modalRef.componentInstance.isDelete = true;

    modalRef.result.then((result) => {
      if (result) {
        this.productService.delete(id).subscribe(() => {
          this.getProducts();
        });
      }
    }).catch(() => {
    });
  }

  ngOnInit(): void {
    this.getProducts();
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getProducts();
  }

  togglePerformances(id: number): void {
    if (this.openedProducts.has(id)) {
      this.openedProducts.delete(id);
    } else {
      this.openedProducts.add(id);
    }
  }

  isOpen(id: number): boolean {
    return this.openedProducts.has(id);
  }

}
