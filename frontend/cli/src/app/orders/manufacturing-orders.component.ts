import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';

import { OrderService } from './manufacturing-order.service';
import { PlanningService } from '../planning/planning.service';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';

type OrderTab = 'TODOS' | 'PENDIENTE' | 'PLANIFICADO' | 'NO_PLANIFICABLE';

@Component({
  selector: 'app-orders',
  imports: [RouterModule, CommonModule, FormsModule, PaginationComponent],
  templateUrl: './manufacturing-orders.html',
  styles: ``
})
export class ManufacturingOrdersComponent implements OnInit {

  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage = 1;
  searchTerm = '';
  activeTab: OrderTab = 'TODOS';
  processingPlanning: boolean = false;

  // Propiedad para enlazar la fecha seleccionada en el input del modal de simulación
  selectedSimulationDate: string = '';

  tabCounts: Record<OrderTab, number> = {
    TODOS: 0, PENDIENTE: 0, PLANIFICADO: 0, NO_PLANIFICABLE: 0
  };

  selectedOrderForModal: any = null;

  constructor(
    private orderService: OrderService,
    private planningService: PlanificacionService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal,
    private toastr: ToastrService,
  ) { }

  ngOnInit(): void {
    this.getOrders();
    this.loadTabCounts();
  }

  // ─── Tabs ────────────────────────────────────────────────────────────────

  setTab(tab: OrderTab): void {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    this.currentPage = 1;
    this.getOrders();
  }

  // ─── Datos ───────────────────────────────────────────────────────────────

  getOrders(): void {
    const state = this.activeTab !== 'TODOS' ? this.activeTab : undefined;
    this.orderService.byPage(this.currentPage, 10, state).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  /** Carga los contadores de las 3 bandejas en paralelo. */
  private loadTabCounts(): void {
    forkJoin({
      pending: this.orderService.byPage(1, 1, 'PENDIENTE'),
      planned: this.orderService.byPage(1, 1, 'PLANIFICADO'),
      unplannable: this.orderService.byPage(1, 1, 'NO_PLANIFICABLE'),
    }).subscribe({
      next: ({ pending, planned, unplannable }) => {
        this.tabCounts.PENDIENTE = (<ResultsPage>pending.data).totalElements;
        this.tabCounts.PLANIFICADO = (<ResultsPage>planned.data).totalElements;
        this.tabCounts.NO_PLANIFICABLE = (<ResultsPage>unplannable.data).totalElements;
        this.tabCounts.TODOS = this.tabCounts.PENDIENTE + this.tabCounts.PLANIFICADO + this.tabCounts.NO_PLANIFICABLE;
        this.cdr.markForCheck();
      },
    });
  }

  /** Refresca tabla Y contadores tras cualquier acción que cambie estados. */
  private refresh(): void {
    this.getOrders();
    this.loadTabCounts();
  }

  // ─── Acciones Masivas (Simulador Temporal) ───────────────────────────────

  abrirModalPlanificacion(content: any): void {
    const ahora = new Date();
    const tzOffset = ahora.getTimezoneOffset() * 60000;

    // Setea por defecto "AAAA-MM-DD" en hora local (ej: "2026-06-05")
    this.selectedSimulationDate = new Date(ahora.getTime() - tzOffset).toISOString().split('T')[0];

    // Abre el modal centrado
    this.modalService.open(content, { centered: true, backdrop: 'static' });
  }

  /** Ejecuta el motor mandando la fecha con las 00:00:00 hardcodeadas */
  executePlanBatch(modalRef: any): void {
    if (!this.selectedSimulationDate) {
      this.toastr.warning('Debe seleccionar una fecha de inicio para la corrida.', 'Atención');
      return;
    }

    this.processingPlanning = true;
    this.cdr.markForCheck();

    // 💡 Magia de Clean Architecture: El usuario ve solo la fecha, 
    // pero a Spring Boot le llega el LocalDateTime perfecto que necesita.
    const fechaFormateadaJava = `${this.selectedSimulationDate}T00:00:00`;

    this.planningService.runMassivePlanning(fechaFormateadaJava).subscribe({
      next: (res) => {
        this.toastr.success('¡Proceso masivo finalizado con éxito!', 'Motor de Planificación');
        this.processingPlanning = false;
        modalRef.close();
        this.refresh();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Ocurrió un error al procesar la asignación de lotes.', 'Error');
        this.processingPlanning = false;
        this.cdr.markForCheck();
      }
    });
  }
  // ─── Acciones Individuales y Filtros ─────────────────────────────────────

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getOrders();
  }

  onSearch(): void {
    this.currentPage = 1;
    if (!this.searchTerm.trim()) { this.getOrders(); return; }

    this.orderService.search(this.searchTerm, 1, 10).subscribe(pkg => {
      this.resultsPage = <ResultsPage>pkg.data;
      this.cdr.markForCheck();
    });
  }

  delete(id: number): void {
    const ref = this.modalService.open(ConfirmModalComponent, { centered: true, backdrop: 'static' });
    ref.componentInstance.title = 'Eliminar Pedido';
    ref.componentInstance.message = '¿Estás seguro de eliminar este pedido? Esta acción no se puede deshacer.';
    ref.componentInstance.btnOkText = 'Sí, Eliminar';
    ref.componentInstance.isDelete = true;

    ref.result.then(ok => { if (ok) this.orderService.delete(id).subscribe(() => this.refresh()); })
      .catch(() => { });
  }

  private planningOrder(orderId: number, modalContent: any): void {
    const startDate = new Date().toISOString().split('T')[0] + 'T00:00:00';

    this.planningService.save({ order: { id: orderId }, startDate }).subscribe({
      next: (dataPackage) => {
        this.refresh();
        const data = <any>dataPackage.data;

        if (Array.isArray(data)) {
          this.toastr.success('¡Pedido planificado con éxito!', 'Éxito');
        }
        else if (data && data.estado === 'NO_PLANIFICABLE') {
          this.toastr.warning('El pedido no pudo planificarse por completo.', 'Planificación Parcial');
          this.openFailureModal(data, modalContent);
        }
      },
      error: err => console.error(err),
    });
  }

  tryPlanningOrder(orderId: number, modalContent: any): void {
    const ref = this.modalService.open(ConfirmModalComponent, { centered: true, backdrop: 'static' });
    ref.componentInstance.title = 'Planificar Pedido';
    ref.componentInstance.message = 'Se generarán las planificaciones para cada producto. ¿Desea continuar?';
    ref.componentInstance.btnOkText = 'Sí, Planificar';
    ref.componentInstance.isDelete = false;

    ref.result.then(ok => {
      if (ok) this.planningOrder(orderId, modalContent);
    }).catch(() => { });
  }

  openFailureModal(order: any, modalContent: any): void {
    this.selectedOrderForModal = order;
    this.modalService.open(modalContent, { centered: true, backdrop: 'static', size: 'lg' });
  }
}