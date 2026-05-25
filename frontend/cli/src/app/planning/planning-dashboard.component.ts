import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WorkshopService } from '../workshops/workshop.service';
import { OrderService } from '../orders/manufacturing-order.service';
import { Workshop } from '../workshops/workshop';
import { manufacturingOrder } from '../orders/manufacturingOrder';
import { PlanningProcess } from '../planning/planning';
import { PlanningService } from './planning.service';

declare var google: any;

@Component({
  selector: 'app-planning-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planning-dashboard.html',
})
export class PlanningDashboardComponent implements OnInit {
  @ViewChild('chartDiv') chartDiv!: ElementRef;

  workshops: Workshop[] = [];
  orders: manufacturingOrder[] = [];

  ordersOfTheDay: any[] = [];
  currentWorkshopName: string = 'Todos los Talleres';

  selectedWorkshopId: string = '';
  selectedOrderId: string = '';
  selectedDate: string = '';

  planningProcesses: PlanningProcess[] = [];
  availableDates: string[] = [];
  loading = false;
  googleChartsLoaded = false;

  constructor(
    private workshopService: WorkshopService,
    private orderService: OrderService,
    private planningService: PlanningService
  ) { }

  ngOnInit(): void {
    this.loadFilterData();

    google.charts.load('current', { packages: ['timeline'] });
    google.charts.setOnLoadCallback(() => {
      this.googleChartsLoaded = true;
      this.fetchAndRenderDashboard();
    });
  }

  loadFilterData(): void {
    this.workshopService.all().subscribe(dataPackage => this.workshops = <Workshop[]>dataPackage.data);
    this.orderService.all().subscribe(dataPackage => this.orders = <manufacturingOrder[]>dataPackage.data);
  }


  onFiltersChange(): void {
    this.selectedDate = '';
    this.fetchAndRenderDashboard();
  }

  fetchAndRenderDashboard(): void {
    if (!this.googleChartsLoaded) return;

    this.loading = true;

    this.planningService.getPlanningsFiltered(this.selectedWorkshopId, this.selectedOrderId).subscribe({
      next: (dataPackage: any) => {
        this.planningProcesses = <PlanningProcess[]>dataPackage.data;

        const dates = new Set<string>();
        this.planningProcesses.forEach(p => p.plannings.forEach(pl => {
          if (pl.period?.start) {
            dates.add(pl.period.start.split('T')[0]);
          }
        }));
        this.availableDates = Array.from(dates).sort();

        if (this.availableDates.length > 0) {
          if (!this.selectedDate || !this.availableDates.includes(this.selectedDate)) {
            this.selectedDate = this.availableDates[0];
          }
        }

        // 2. CALCULAMOS LAS PROPIEDADES ANTES DE APAGAR EL LOADING
        this.computeWorkshopName();
        this.computeOrdersOfTheDay();

        this.loading = false;

        setTimeout(() => {
          this.renderChart();
        }, 50);
      },
      error: (err) => {
        console.error('Error al recuperar tablero de planificación:', err);
        this.loading = false;
      }
    });
  }

  renderChart(): void {
    if (!this.planningProcesses || this.planningProcesses.length === 0 || !this.chartDiv || !this.selectedDate) return;

    const processPalette = ['#3366cc', '#dc3912', '#ff9900', '#109618', '#990099', '#0099c6', '#dd4477', '#66aa00'];
    const dataTable = new google.visualization.DataTable();

    dataTable.addColumn({ type: 'string', id: 'Equipo' });
    dataTable.addColumn({ type: 'string', id: 'Proceso' });
    dataTable.addColumn({ type: 'string', role: 'tooltip', p: { html: true } });
    dataTable.addColumn({ type: 'date', id: 'Inicio' });
    dataTable.addColumn({ type: 'date', id: 'Fin' });

    const rows: any[] = [];
    const colors: string[] = [];
    const uniqueEquipments = new Set<string>();

    this.planningProcesses.forEach((process, processIndex) => {
      const color = processPalette[processIndex % processPalette.length];
      colors.push(color);
      const processLabel = `Proceso #${process.id}`;

      process.plannings.forEach(planning => {
        const start = new Date(planning.period.start);
        const end = new Date(planning.period.endDate);
        const startDateStr = planning.period.start.split('T')[0];

        // Filtro por fecha seleccionada del carrusel
        if (startDateStr !== this.selectedDate) return;
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

        const taskName = planning.task?.name || 'Tarea';
        const equipCode = planning.equipment?.code || 'S/E';
        uniqueEquipments.add(equipCode);

        const tooltip = `
                    <div class="p-3" style="font-size:13px; min-width: 180px;">
                        <b style="color: ${color};">${processLabel}</b><br/>
                        <b>Tarea:</b> ${taskName}<br/>
                        <b>Equipo:</b> ${equipCode}<br/>
                        <b>Duración:</b> ${planning.task?.duration || 0} min<br/>
                        <b>Inicio:</b> ${start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}<br/>
                        <b>Fin:</b> ${end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}<br/>
                    </div>`;

        rows.push([equipCode, { v: processLabel, f: '' }, tooltip, start, end]);
      });
    });

    if (rows.length === 0) {
      this.chartDiv.nativeElement.innerHTML = '<div class="text-muted p-4 text-center">No hay ejecuciones de tareas agendadas para este día.</div>';
      return;
    }

    dataTable.addRows(rows);


    let totalRowsToDisplay = uniqueEquipments.size;

    if (this.selectedWorkshopId === '' && this.workshops.length > 0) {
      const totalEquipmentsInSystem = this.workshops.reduce((acc, w) => acc + (w.equipments?.length || 0), 0);
      // Usamos el máximo entre los equipos con tareas y el total estimado (o un número base seguro como 8)
      totalRowsToDisplay = Math.max(uniqueEquipments.size, totalEquipmentsInSystem, 8);
    } else if (totalRowsToDisplay === 1) {
      totalRowsToDisplay = 2;
    }

    const calculatedHeight = Math.max(220, (totalRowsToDisplay * 42) + 60);

    const options = {
      height: calculatedHeight,
      colors,
      tooltip: { isHtml: true },
      timeline: {
        showRowLabels: true,
        groupByRowLabel: true,
        colorByRowLabel: false,
        rowLabelStyle: { fontSize: 12, color: '#475569' },
        barLabelStyle: { fontSize: 11 }
      },
      animation: { duration: 0 }
    };

    const chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
    chart.draw(dataTable, options);
  }

  private computeWorkshopName(): void {
    const workshop = this.workshops.find(w => w.id.toString() === this.selectedWorkshopId);
    this.currentWorkshopName = workshop ? `${workshop.name} (${workshop.code})` : 'Todos los Talleres';
  }

  // REFACTOR: Cálculo único de las órdenes del día (Se ejecuta solo al cambiar datos o de fecha)
  computeOrdersOfTheDay(): void {
    if (!this.planningProcesses || this.planningProcesses.length === 0 || !this.selectedDate) {
      this.ordersOfTheDay = [];
      return;
    }

    const activeProcesses = this.planningProcesses.filter(process =>
      process.plannings?.some(planning => planning.period?.start?.split('T')[0] === this.selectedDate)
    );

    const ordersMap = new Map<number, any>();

    activeProcesses.forEach(proc => {
      if (!proc.order || !proc.order.id) return;

      const orderId = proc.order.id;

      if (!ordersMap.has(orderId)) {
        let productDisplay = 'Producto No Especificado';
        if (proc.order.product) {
          productDisplay = proc.order.product.name; //hola
        }

        ordersMap.set(orderId, {
          order: proc.order,
          resolvedProductName: productDisplay,
          customerName: proc.order.customer?.companyName ,
          subProcessesCount: 1
        });
      } else {
        const existingOrder = ordersMap.get(orderId);
        existingOrder.subProcessesCount++;
      }
    });

    this.ordersOfTheDay = Array.from(ordersMap.values());
  }

  // Recuerda actualizar las órdenes también cuando el usuario cambia de día en el carrusel
  changeDate(direction: 'prev' | 'next'): void {
    const currentIndex = this.availableDates.indexOf(this.selectedDate);
    if (direction === 'prev' && currentIndex > 0) {
      this.selectedDate = this.availableDates[currentIndex - 1];
    } else if (direction === 'next' && currentIndex < this.availableDates.length - 1) {
      this.selectedDate = this.availableDates[currentIndex + 1];
    }

    // Al cambiar la fecha, recalculamos las órdenes correspondientes antes de redibujar el gráfico
    this.computeOrdersOfTheDay();

    setTimeout(() => this.renderChart(), 0);
  }

  getWorkshopName(): string {
    const workshop = this.workshops.find(w => w.id.toString() === this.selectedWorkshopId);
    return workshop ? `${workshop.name} (${workshop.code})` : 'Todos los Talleres';
  }

  getOrdersOfTheDay(): any[] {
    if (!this.planningProcesses || this.planningProcesses.length === 0 || !this.selectedDate) {
      return [];
    }

    const activeProcesses = this.planningProcesses.filter(process =>
      process.plannings?.some(planning => planning.period?.start?.split('T')[0] === this.selectedDate)
    );

    const ordersMap = new Map<number, any>();

    activeProcesses.forEach(proc => {
      if (!proc.order || !proc.order.id) return;

      const orderId = proc.order.id;

      if (!ordersMap.has(orderId)) {
        let productDisplay = 'Producto No Especificado';
        if (proc.order.product) {
          productDisplay = proc.order.product.name;
        }

        ordersMap.set(orderId, {
          order: proc.order,
          resolvedProductName: productDisplay,
          customerName: proc.order.customer?.companyName,
          subProcessesCount: 1
        });
      } else {
        const existingOrder = ordersMap.get(orderId);
        existingOrder.subProcessesCount++;
      }
    });

    return Array.from(ordersMap.values());
  }

  isFirstDate(): boolean { return this.availableDates.indexOf(this.selectedDate) === 0; }
  isLastDate(): boolean { return this.availableDates.indexOf(this.selectedDate) === this.availableDates.length - 1; }
}