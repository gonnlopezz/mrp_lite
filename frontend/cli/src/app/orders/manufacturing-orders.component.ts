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
import { PlanningService } from '../planning/planning.service';

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
    private planningService: PlanningService,
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



  tryPlanningOrder(orderId: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      centered: true,
      backdrop: 'static'
    });

    modalRef.componentInstance.title = 'Planificar Pedido';
    modalRef.componentInstance.message = 'Se realizará las planificaciones para cada producto. ¿Desea continuar?';
    modalRef.componentInstance.btnOkText = 'Sí, Planificar';
    modalRef.componentInstance.isDelete = false;

    modalRef.result.then((result) => {
      if (result) {
        this.planningOrder(orderId);
      }
    }).catch(() => {
    });

  }

  private planningOrder(orderId: number): void {
    const currentDate = new Date().toISOString().split('T')[0] + 'T00:00:00';
    const payload = {
      order: {
        id: orderId
      },
      startDate: currentDate
    };

    this.planningService.save(payload).subscribe({
      next: (response) => this.onSearch(),
      error: (err) => console.error(err)
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
