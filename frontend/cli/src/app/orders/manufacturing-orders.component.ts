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
    // Implementar búsqueda cuando sea necesario
    // Por ahora, recarga la página 1
    this.currentPage = 1;
    this.getOrders();
  }

  toggleDetails(id: number): void {
    if (this.openedOrders.has(id)) {
      this.openedOrders.delete(id);
    } else {
      this.openedOrders.add(id);
    }
  }

  isOpen(id: number): boolean {
    return this.openedOrders.has(id);
  }


  getOrders(): void {
    this.orderService.byPage(this.currentPage, 8).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

}
