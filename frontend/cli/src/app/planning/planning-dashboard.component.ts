import { Component, OnInit, AfterViewInit, ViewChildren, QueryList, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { WorkshopService } from '../workshops/workshop.service';
import { OrderService } from '../orders/manufacturing-order.service';
import { PlanningService } from './planning.service';
import { Workshop } from '../workshops/workshop';
import { manufacturingOrder } from '../orders/manufacturingOrder';
import { PlanningProcess } from '../planning/planning';
import { ChartRow, ColorMode, DayOrderSummary, WorkshopChartBlock } from './planning-dasboard';


declare var google: any;

const PROCESS_PALETTE = [
  '#3366cc', '#dc3912', '#ff9900', '#109618',
  '#990099', '#0099c6', '#dd4477', '#66aa00'
];

@Component({
  selector: 'app-planning-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planning-dashboard.html',
})
export class PlanningDashboardComponent implements OnInit, AfterViewInit {

  @ViewChildren('chartDiv') chartDivs!: QueryList<ElementRef>;

  private equipmentWorkshopMap = new Map<number, { code: string; name: string }>();

  workshops: Workshop[] = [];
  orders: manufacturingOrder[] = [];
  planningProcesses: PlanningProcess[] = [];

  selectedWorkshopId: string = '';
  selectedOrderId: string = '';
  selectedDate: string = '';
  colorMode: ColorMode = 'by-process';

  availableDates: string[] = [];
  ordersOfTheDay: DayOrderSummary[] = [];
  workshopBlocks: WorkshopChartBlock[] = [];

  loading = false;
  googleChartsLoaded = false;

  constructor(
    private workshopService: WorkshopService,
    private orderService: OrderService,
    private planningService: PlanningService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
  // Los selects se cargan UNA sola vez
  this.loadSelectData();

  google.charts.load('current', { packages: ['timeline'] });
  google.charts.setOnLoadCallback(() => {
    this.googleChartsLoaded = true;
    this.fetchAndRender();
  });
}

  ngAfterViewInit(): void {
    // Se dispara cada vez que el *ngFor agrega/quita divs del DOM
    this.chartDivs.changes.subscribe((list: QueryList<ElementRef>) => {
      if (list.length > 0 && this.workshopBlocks.length > 0) {
        this.drawCharts(list.toArray());
      }
    });
  }



  // ─── Entry point: se llama al iniciar y al cambiar filtros ───────────────

  onFiltersChange(): void {
    this.selectedDate = '';   // Resetea fecha al cambiar taller u orden
    this.fetchAndRender();
  }

  loadSelectData(): void {
  forkJoin({
    workshops: this.workshopService.all(),
    orders:    this.orderService.all()
  }).subscribe({
    next: ({ workshops, orders }) => {
      this.workshops = workshops.data as Workshop[];
      this.orders    = orders.data as manufacturingOrder[];
      // El mapa se puede construir aquí ya que no depende de planificaciones
      this.buildEquipmentWorkshopMap();
    },
    error: (err) => console.error('Error cargando filtros:', err)
  });
}

  fetchAndRender(): void {
  if (!this.googleChartsLoaded) return;

  this.loading = true;

  this.planningService
    .getPlanningsFiltered(this.selectedWorkshopId, this.selectedOrderId)
    .subscribe({
      next: (dataPackage: any) => {
        this.planningProcesses = dataPackage.data as PlanningProcess[];

        this.computeAvailableDates();
        this.computeOrdersOfTheDay();
        this.computeWorkshopBlocks();

        this.loading = false;
        this.renderAllCharts();
      },
      error: (err) => {
        console.error('Error cargando planificaciones:', err);
        this.loading = false;
      }
    });
}

  // ─── Extrae las fechas únicas con planificaciones ────────────────────────

  private computeAvailableDates(): void {
    const dates = new Set<string>();

    this.planningProcesses.forEach(process =>
      process.plannings?.forEach(planning => {
        const dateStr = planning.period?.start?.split('T')[0];
        if (dateStr) dates.add(dateStr);
      })
    );

    this.availableDates = Array.from(dates).sort();

    // Mantiene la fecha seleccionada si sigue siendo válida, si no toma la primera
    if (this.availableDates.length === 0) {
      this.selectedDate = '';
      return;
    }

    const dateStillValid = this.availableDates.includes(this.selectedDate);
    if (!dateStillValid) {
      this.selectedDate = this.availableDates[0];
    }
  }

  // ─── Calcula el resumen de órdenes activas en la fecha seleccionada ──────

  private computeOrdersOfTheDay(): void {
    if (!this.selectedDate || this.planningProcesses.length === 0) {
      this.ordersOfTheDay = [];
      return;
    }

    const ordersMap = new Map<number, DayOrderSummary>();

    this.planningProcesses.forEach(process => {
      const order = process.order;
      if (!order?.id) return;

      const activeWorkshopNames = (process.plannings ?? [])
        .filter(p => p.period?.start?.split('T')[0] === this.selectedDate)
        .map(p => {
          const ws = this.equipmentWorkshopMap.get(p.equipment?.id);
          return ws?.name ?? null;
        })
        .filter((name): name is string => !!name);

      if (activeWorkshopNames.length === 0) return;

      if (!ordersMap.has(order.id)) {
        ordersMap.set(order.id, {
          orderId: order.id,
          orderLabel: `Orden #${order.id}`,
          customerName: order.customer?.companyName ?? 'Cliente',
          productName: order.product?.name ?? 'Producto no especificado',
          processCount: 1,
          workshopNames: [...new Set(activeWorkshopNames)]
        });
      } else {
        const existing = ordersMap.get(order.id)!;
        existing.processCount++;
        activeWorkshopNames.forEach(name => {
          if (!existing.workshopNames.includes(name)) {
            existing.workshopNames.push(name);
          }
        });
      }
    });

    this.ordersOfTheDay = Array.from(ordersMap.values());
  }
  // ─── Arma los bloques de chart: uno por taller visible ──────────────────

  private computeWorkshopBlocks(): void {
    if (!this.selectedDate || this.planningProcesses.length === 0) {
      this.workshopBlocks = [];
      return;
    }

    const colorMap = this.buildColorMap();
    const blocksMap = new Map<string, WorkshopChartBlock>();

    this.planningProcesses.forEach(process => {
      const color = colorMap.get(this.colorKey(process)) ?? '#999999';
      const processLabel = `Proceso #${process.id}`;

      process.plannings?.forEach(planning => {
        const dateStr = planning.period?.start?.split('T')[0];
        if (dateStr !== this.selectedDate) return;

        const start = new Date(planning.period.start);
        const end = new Date(planning.period.endDate);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

        const equipmentId = planning.equipment?.id;
        const workshop = this.equipmentWorkshopMap.get(equipmentId);

        // Si el equipo no pertenece a ningún taller conocido, lo ignoramos
        if (!workshop) return;

        const equipCode = planning.equipment?.code ?? 'S/E';
        const taskName = planning.task?.name ?? 'Tarea';
        const duration = planning.task?.duration ?? 0;

        const tooltip = this.buildTooltip(
          processLabel, taskName, equipCode, duration, start, end, color
        );

        const row: ChartRow = {
          equipmentCode: equipCode,
          rowLabel: processLabel,
          tooltip,
          start,
          end,
          color
        };

        if (!blocksMap.has(workshop.code)) {
          blocksMap.set(workshop.code, {
            workshopName: workshop.name,
            workshopCode: workshop.code,
            rows: []
          });
        }
        blocksMap.get(workshop.code)!.rows.push(row);
      });
    });

    this.workshopBlocks = Array.from(blocksMap.values());
  }

  private buildEquipmentWorkshopMap(): void {
    this.equipmentWorkshopMap.clear();
    this.workshops.forEach(workshop => {
      workshop.equipments?.forEach(equipment => {
        this.equipmentWorkshopMap.set(equipment.id, {
          code: workshop.code,
          name: workshop.name
        });
      });
    });
  }

  // ─── Construye el mapa de colores según el modo seleccionado ────────────

  private buildColorMap(): Map<string, string> {
    const colorMap = new Map<string, string>();
    let index = 0;

    this.planningProcesses.forEach(process => {
      const key = this.colorKey(process);
      if (!colorMap.has(key)) {
        colorMap.set(key, PROCESS_PALETTE[index % PROCESS_PALETTE.length]);
        index++;
      }
    });

    return colorMap;
  }

  // La clave de color cambia según el modo: id del proceso o id de la orden
  private colorKey(process: PlanningProcess): string {
    if (this.colorMode === 'by-order') {
      return `order-${process.order?.id ?? 'unknown'}`;
    }
    return `process-${process.id}`;
  }

  // ─── Tooltip HTML reutilizable ───────────────────────────────────────────

  private buildTooltip(
    processLabel: string,
    taskName: string,
    equipCode: string,
    duration: number,
    start: Date,
    end: Date,
    color: string
  ): string {
    const fmt = (d: Date) => d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    return `
      <div style="padding:10px; font-size:13px; min-width:180px; font-family:sans-serif;">
        <b style="color:${color};">${processLabel}</b><br/>
        <b>Tarea:</b> ${taskName}<br/>
        <b>Equipo:</b> ${equipCode}<br/>
        <b>Duración:</b> ${duration} min<br/>
        <b>Inicio:</b> ${fmt(start)}<br/>
        <b>Fin:</b> ${fmt(end)}<br/>
      </div>`;
  }

  // ─── Cambia la fecha del carrusel ────────────────────────────────────────

  changeDate(direction: 'prev' | 'next'): void {
    const currentIndex = this.availableDates.indexOf(this.selectedDate);
    if (direction === 'prev' && currentIndex > 0) {
      this.selectedDate = this.availableDates[currentIndex - 1];
    } else if (direction === 'next' && currentIndex < this.availableDates.length - 1) {
      this.selectedDate = this.availableDates[currentIndex + 1];
    }

    // Al cambiar fecha solo recalculamos los derivados, no volvemos a llamar al backend
    this.computeOrdersOfTheDay();
    this.computeWorkshopBlocks();
    this.cdr.detectChanges();
    this.renderAllCharts();
  }

  // ─── Cambia el modo de color y recalcula sin ir al backend ──────────────

  onColorModeChange(): void {
    this.computeWorkshopBlocks();
    this.cdr.detectChanges();
    this.renderAllCharts();
  }

  setColorMode(mode: ColorMode): void {
    if (this.colorMode === mode) return;
    this.colorMode = mode;
    this.onColorModeChange();
  }
  // ─── Renderizado de charts (Paso 4, por ahora placeholder) ──────────────


  renderAllCharts(): void {
    if (!this.googleChartsLoaded || !this.hasData) return;
    this.cdr.detectChanges();
  }

  // La lógica real de dibujo se mueve aquí
  private drawCharts(divs: ElementRef[]): void {
    this.workshopBlocks.forEach((block, index) => {
      const divRef = divs[index];
      if (!divRef) return;
      this.renderChartForBlock(block, divRef.nativeElement);
    });
  }

  private renderChartForBlock(block: WorkshopChartBlock, container: HTMLElement): void {
    if (block.rows.length === 0) {
      container.innerHTML = `
      <div class="text-muted small text-center py-3">
        No hay tareas planificadas para este taller en la fecha seleccionada.
      </div>`;
      return;
    }




    const { dataTable, colors } = this.buildDataTable(block.rows);
    const options = this.buildChartOptions(block.rows, colors);
    const chart = new google.visualization.Timeline(container);

    chart.draw(dataTable, options);
  }

  private buildDataTable(rows: ChartRow[]): { dataTable: any; colors: string[] } {
    const dataTable = new google.visualization.DataTable();

    dataTable.addColumn({ type: 'string', id: 'Equipo' });
    dataTable.addColumn({ type: 'string', id: 'Proceso' });
    dataTable.addColumn({ type: 'string', role: 'tooltip', p: { html: true } });
    dataTable.addColumn({ type: 'date', id: 'Inicio' });
    dataTable.addColumn({ type: 'date', id: 'Fin' });

    // Google Charts asigna colores por orden de aparición de rowLabel único.
    // Para respetar nuestro colorMap, necesitamos que cada rowLabel
    // tenga un color asociado en el mismo orden en que aparece.
    const colorOrder: string[] = [];
    const seenLabels = new Set<string>();

    rows.forEach(row => {
      dataTable.addRow([
        row.equipmentCode,
        { v: row.rowLabel, f: '' },
        row.tooltip,
        row.start,
        row.end
      ]);

      // Registramos el color la primera vez que aparece ese label
      if (!seenLabels.has(row.rowLabel)) {
        seenLabels.add(row.rowLabel);
        colorOrder.push(row.color);
      }
    });

    return { dataTable, colors: colorOrder };
  }

  private buildChartOptions(rows: ChartRow[], colors: string[]): object {
    const uniqueEquipments = new Set(rows.map(r => r.equipmentCode)).size;

    // 41px por fila de equipo + 50px de encabezado del chart es lo que Google Charts necesita
    const ROW_HEIGHT = 41;
    const HEADER_HEIGHT = 50;
    const MIN_HEIGHT = 120;

    const height = Math.max(MIN_HEIGHT, uniqueEquipments * ROW_HEIGHT + HEADER_HEIGHT);

    return {
      height,
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
  }

  // ─── Getters de template ─────────────────────────────────────────────────

  get isFirstDate(): boolean {
    return this.availableDates.indexOf(this.selectedDate) === 0;
  }

  get isLastDate(): boolean {
    return this.availableDates.indexOf(this.selectedDate) === this.availableDates.length - 1;
  }

  get currentWorkshopLabel(): string {
    const w = this.workshops.find(w => w.id.toString() === this.selectedWorkshopId);
    return w ? `${w.name} (${w.code})` : 'Todos los Talleres';
  }

  get hasData(): boolean {
    return this.planningProcesses.length > 0 && !!this.selectedDate;
  }
}