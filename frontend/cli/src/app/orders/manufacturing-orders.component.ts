import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { manufacturingOrder } from './manufacturingOrder';
import { OrderService } from './manufacturing-order.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';

@Component({
  selector: 'app-orders',
  imports: [RouterModule, CommonModule, FormsModule, PaginationComponent],
  templateUrl: './manufacturing-orders.html',
  styles: ``
})
export class ManufacturingOrdersComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  searchTerm: string = '';
  openedOrders: Set<number> = new Set();

  constructor(
    private orderService: OrderService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal) { }

  delete(id: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      centered: true,
      backdrop: 'static'
    });

    modalRef.componentInstance.title = 'Eliminar Pedido';
    modalRef.componentInstance.message = `¿Estás seguro de eliminar este pedido de fabricación? Esta acción no se puede deshacer.`;
    modalRef.componentInstance.btnOkText = 'Sí, Eliminar';
    modalRef.componentInstance.isDelete = true;

    modalRef.result.then((result) => {
      if (result) {
        this.orderService.delete(id).subscribe(() => {
          this.getOrders();
        });
      }
    }).catch(() => {
    });
  }

  ngOnInit(): void {
    this.getOrders();
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getOrders();
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.currentPage = 1;
      this.getOrders();
      return;
    }

    this.currentPage = 1;
    this.orderService.search(this.searchTerm, this.currentPage, 10).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }


  getOrders(): void {
    this.orderService.byPage(this.currentPage, 10).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

}
