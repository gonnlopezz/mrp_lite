// planning-dashboard.component.ts
import {
  Component, OnInit, AfterViewInit,
  ViewChildren, QueryList, ElementRef, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { WorkshopService } from '../workshops/workshop.service';
import { OrderService } from '../orders/manufacturing-order.service';
import { PlanningService } from './planning.service';
import { PlanningChartService } from './planning-chart.service';   // <-- nuevo
import { Taller } from '../workshops/workshop';
import { PedidoFabricacion } from '../orders/manufacturingOrder';
import { Producto } from '../products/product';
import { ProcesoPlanificacion } from '../planning/planning';
import { ColorMode, ChartRow, WorkshopChartBlock, DayOrderSummary, DayProductSummary } from './planning-dashboard';
import { productService } from '../products/product.service';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { NgbDate, NgbDateStruct, NgbDatepickerModule, NgbDropdownModule, NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';

const PROCESS_PALETTE = [
  '#3366cc', '#dc3912', '#ff9900', '#109618',
  '#990099', '#0099c6', '#dd4477', '#66aa00'
];

declare var google: any;

@Component({
  selector: 'app-planning-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NgbDatepickerModule, NgbDropdownModule, NgbTypeaheadModule],
  templateUrl: './planning-dashboard.html',
  providers: [PlanningChartService]
})
export class PlanningDashboardComponent implements OnInit, AfterViewInit {

  @ViewChildren('chartDiv') chartDivs!: QueryList<ElementRef>;

  workshops: Taller[] = [];
  orders: PedidoFabricacion[] = [];
  planningProcesses: PlanificacionProcess[] = [];

  selectedWorkshopId: string = '';
  selectedOrderId: string = '';
  selectedOrderObject: PedidoFabricacion | null = null;
  selectedDate: string = '';
  colorMode: ColorMode = 'by-process';

  availableDates: string[] = [];
  workshopBlocks: TallerChartBlock[] = [];
  dayTimeRange: { min: Date; max: Date } | null = null;

  loading: boolean = false;
  renderingCharts: boolean = false;
  googleChartsLoaded = false;

  private equipmentWorkshopMap = new Map<number, { código: string; name: string }>();
  private taskProductMap = new Map<number, string>();

  selectedShifts: Record<string, string> = {};

  readonly SHIFT_RANGES: Record<string, { start: number; end: number }> = {
    all: { start: 0, end: 1440 },  // 24 hs completas
    night: { start: 0, end: 480 },  // 00:00 a 08:00 (Noche / Madrugada)
    morning: { start: 480, end: 960 },  // 08:00 a 16:00 (Mañana)
    afternoon: { start: 960, end: 1440 }   // 16:00 a 24:00 (Tarde / Cierre)
  };

  constructor(
    private workshopService: TallerService,
    private orderService: OrderService,
    private productService: productService,
    private planningService: PlanificacionService,
    private chartService: PlanificacionChartService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadSelectData();
    google.charts.load('current', { packages: ['timeline'] });
    google.charts.setOnLoadCallback(() => {
      this.googleChartsLoaded = true;
      this.fetchAndRender();
    });
  }

  ngAfterViewInit(): void {
    this.chartDivs.changes.subscribe((list: QueryList<ElementRef>) => {
      if (list.length > 0 && this.workshopBlocks.length > 0) {
        this.drawCharts(list.toArray());
      }
    });
  }

  // ─── Carga de selects ────────────────────────────────────────────────────

  loadSelectData(): void {
    forkJoin({
      workshops: this.workshopService.all(),
      orders: this.orderService.allPlanned(),
      products: this.productoService.all()
    }).subscribe({
      next: ({ workshops, orders, products }) => {
        this.workshops = workshops.data as Workshop[];
        this.orders = orders.data as manufacturingOrder[];
        this.buildEquipmentWorkshopMap();
        this.buildTaskProductMap(products.data as Product[]);
      },
      error: (err) => console.error('Error cargando filtros:', err)
    });
  }

  // ─── Lógica de Typeahead para Pedidos (Formato Solicitado) ────────────────

  searchOrders = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(term =>
        term.length < 1
          ? of([])
          : of(this.orders.filter(o =>
            o.id.toString().includes(term.toLowerCase()) ||
            (o.cliente?.razónSocial || '').toLowerCase().includes(term.toLowerCase())
          )).pipe(
            map(results => results.slice(0, 10))
          )
      ),
      catchError(() => of([]))
    );

  orderInputFormatter = (order: PedidoFabricacion): string => {
    return order ? `Orden #${order.id} — ${order.cliente?.razónSocial || 'Cliente'}` : '';
  };

  orderResultFormatter = (order: PedidoFabricacion): string => {
    return `Orden #${order.id} — ${order.cliente?.razónSocial || 'Cliente'}`;
  };

  // ─── Fetch principal ─────────────────────────────────────────────────────

  onFiltersChange(): void {
    this.fetchAndRender();
  }

  onOrderSelected(event: any): void {
    this.selectedOrderObject = event.item;
    this.selectedOrderId = this.selectedOrderObject ? this.selectedOrderObject.id.toString() : '';
    this.onFiltersChange();
  }

  fetchAndRender(): void {
    if (!this.googleChartsLoaded) return;
    this.loading = true;

    this.planningService
      .getPlanningsFiltered(this.selectedWorkshopId, this.selectedOrderId)
      .subscribe({
        next: dataPackage => {
          this.planningProcesses = dataPackage.data as PlanningProcess[];
          this.computeAvailableDates();
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

  // ─── Carrusel ────────────────────────────────────────────────────────────

  changeDate(direction: 'prev' | 'next'): void {
    const index = this.availableDates.indexOf(this.selectedDate);
    if (direction === 'prev' && index > 0)
      this.selectedDate = this.availableDates[index - 1];
    else if (direction === 'next' && index < this.availableDates.length - 1)
      this.selectedDate = this.availableDates[index + 1];

    this.renderingCharts = true;
    this.computeWorkshopBlocks();
    this.renderAllCharts();
  }

  // ─── Color mode ──────────────────────────────────────────────────────────

  setColorMode(mode: ColorMode): void {
    if (this.colorMode === mode) return;
    this.colorMode = mode;
    this.computeWorkshopBlocks();
    this.renderAllCharts();
  }

  // ─── Filtros ─────────────────────────────────────────────────────────────

  resetFilters(): void {
    this.selectedWorkshopId = '';
    this.selectedOrderId = '';
    this.selectedOrderObject = null; // <-- Limpieza física del input text
    this.selectedDate = '';
    this.selectedShifts = {};
    this.onFiltersChange();
  }

  // ─── Renderizado ─────────────────────────────────────────────────────────

  renderAllCharts(): void {
  if (!this.googleChartsLoaded || !this.hasData) return;

  // 1. Forzamos el acoplamiento de estados en el ciclo de Angular
  this.cdr.detectChanges();

  // 2. Le damos un slot mínimo al event loop para asegurar que el DOM esté listo
  setTimeout(() => {
    if (this.chartDivs && this.chartDivs.length > 0) {
      this.drawCharts(this.chartDivs.toArray());
    }
  }, 50);
}

  private drawCharts(divs: ElementRef[]): void {
  this.workshopBlocks.forEach((block, index) => {
    const divRef = divs[index];
    if (!divRef) return;

    // 1. Limpieza física segura del contenedor para forzar un lienzo fresco
    const container = divRef.nativeElement;
    container.innerHTML = '';

    // 2. Creamos un nodo hijo interno para que Google dibuje sin romper la referencia nativa de Angular
    const chartTarget = document.createElement('div');
    container.appendChild(chartTarget);

    const currentShift = this.getShiftForWorkshop(block.workshopCode);
    const range = this.SHIFT_RANGES[currentShift];

    const shiftMin = new Date(this.selectedDate + 'T00:00:00');
    shiftMin.setMinutes(shiftMin.getMinutes() + range.inicio);

    const shiftMax = new Date(this.selectedDate + 'T00:00:00');
    shiftMax.setMinutes(shiftMax.getMinutes() + range.end);

    // 3. Pasamos el nuevo nodo hijo seguro al servicio
    this.chartService.drawBlock(
      block,
      chartTarget,
      (block as any).timeRange,
      currentShift === 'all' ? null : { min: shiftMin, max: shiftMax }
    );
  });
  
  this.renderingCharts = false;
  this.cdr.detectChanges();
}

  // ─── Mapas de lookup ─────────────────────────────────────────────────────

  private buildEquipmentWorkshopMap(): void {
    this.equipoWorkshopMap.clear();
    this.workshops.forEach(workshop =>
      workshop.equipos?.forEach(equipment =>
        this.equipoWorkshopMap.set(equipment.id, {
          código: workshop.código,
          name: workshop.nombre
        })
      )
    );
  }

  private buildTaskProductMap(products: Producto[]): void {
    this.tareaProductMap.clear();
    products.forEach(product =>
      product.tareas?.forEach(task =>
        this.tareaProductMap.set(task.id, product.nombre)
      )
    );
  }

  // ─── Compute ─────────────────────────────────────────────────────────────

  private computeAvailableDates(): void {
    const dates = new Set<string>();
    this.planningProcesses.forEach(process =>
      process.planificaciones?.forEach(planning => {
        const dateStr = planning.periodo?.inicio?.split('T')[0];
        if (dateStr) dates.add(dateStr);
      })
    );
    this.availableDates = Array.from(dates).sort();

    if (this.availableDates.length === 0) { this.selectedDate = ''; return; }
    if (!this.availableDates.includes(this.selectedDate))
      this.selectedDate = this.availableDates[0];
  }

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

      process.planificaciones?.forEach(planning => {
        const dateStr = planning.periodo?.inicio?.split('T')[0];
        if (dateStr !== this.selectedDate) return;

        const start = new Date(planning.periodo.inicio);
        const end = new Date(planning.periodo.fin);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

        const workshop = this.equipoWorkshopMap.get(planning.equipo?.id);
        if (!workshop) return;

        const row: ChartRow = {
          equipmentCode: planning.equipo?.código ?? 'S/E',
          rowLabel: processLabel,
          tooltip: this.buildTooltip(processLabel, planning.tarea?.name ?? 'Tarea', planning.equipo?.código ?? 'S/E', start, end, color),
          start, end, color
        };

        if (!blocksMap.has(workshop.código)) {
          blocksMap.set(workshop.código, {
            workshopName: workshop.nombre, workshopCode: workshop.código,
            rows: [], ordersOfTheDay: [], productsOfTheDay: []
          });
          // Lo tratamos como any temporalmente para asignarle el rango dinámico
          (blocksMap.get(workshop.código) as any).timeRange = { min: start, max: end };
        } else {
          const block = blocksMap.get(workshop.código) as any;
          if (start < block.timeRange.min) block.timeRange.min = start;
          if (end > block.timeRange.max) block.timeRange.max = end;
        }

        blocksMap.get(workshop.código)!.rows.push(row);
      });
    });

    // El resto del método se mantiene igual mapeando los summaries...
    blocksMap.forEach((block, workshopCode) => {
      const { orders, products } = this.computeSummaryForWorkshop(workshopCode);
      block.ordersOfTheDay = orders;
      block.productosOfTheDay = products;
    });

    this.workshopBlocks = Array.from(blocksMap.values());

    // ⚠️ ELIMINAMOS la llamada a 'this.computeDayTimeRange()' ya no es necesaria globalmente
  }

  private computeSummaryForWorkshop(workshopCode: string): {
    orders: DayOrderSummary[]; products: DayProductSummary[];
  } {
    const ordersMap = new Map<number, DayOrderSummary>();
    const productsMap = new Map<string, DayProductSummary>();

    this.planningProcesses.forEach(process => {
      const activeHere = process.planificaciones?.filter(p => {
        const dateStr = p.periodo?.inicio?.split('T')[0];
        const workshop = this.equipoWorkshopMap.get(p.equipo?.id);
        return dateStr === this.selectedDate && workshop?.código === workshopCode;
      }) ?? [];

      if (activeHere.length === 0) return;

      if (process.order?.id) {
        const order = process.order;
        if (!ordersMap.has(order.id)) {
          ordersMap.set(order.id, {
            orderId: order.id,
            orderLabel: `Orden #${order.id}`,
            customerName: order.cliente?.razónSocial ?? 'Cliente',
            productName: order.producto?.nombre ?? 'Sin producto',
            cantidad: order.cantidad ?? 0,
            processCount: 1,
            workshopNames: [workshopCode]
          });
        } else {
          ordersMap.get(order.id)!.processCount++;
        }
      } else {
        const taskId = activeHere[0].tarea?.id;
        const productName = taskId ? this.tareaProductMap.get(taskId) : null;
        if (!productName) return;

        if (!productsMap.has(productName)) {
          productsMap.set(productName, { productName, processCount: 1 });
        } else {
          productsMap.get(productName)!.processCount++;
        }
      }
    });

    return {
      orders: Array.from(ordersMap.values()),
      products: Array.from(productsMap.values())
    };
  }

  // ─── Helpers privados ────────────────────────────────────────────────────

  private buildColorMap(): Map<string, string> {
    const map = new Map<string, string>();
    let index = 0;
    this.planningProcesses.forEach(process => {
      const key = this.colorKey(process);
      if (!map.has(key)) map.set(key, PROCESS_PALETTE[index++ % PROCESS_PALETTE.length]);
    });
    return map;
  }

  private colorKey(process: PlanificacionProcess): string {
    return this.colorMode === 'by-order'
      ? `order-${process.order?.id ?? 'unknown'}`
      : `process-${process.id}`;
  }

  private buildTooltip(
    processLabel: string,
    taskName: string,
    equipCode: string,
    start: Date,
    end: Date,
    color: string,
    orderLabel?: string
  ): string {
    const fmt = (d: Date) => d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const durationMinutes = Math.round((end.getTime() - start.getTime()) / 60000);

    return `
    <div style="padding: 12px; font-size: 13px; min-width: 220px; font-family: sans-serif; white-space: nowrap;">
      <b style="color:${color};">${processLabel}</b><br/>
      ${orderLabel ? `<b>Orden:</b> ${orderLabel}<br/>` : ''}
      <b>Tarea:</b> ${taskName}<br/>
      <b>Equipo:</b> ${equipCode}<br/>
      <b>Duración:</b> ${durationMinutes} min<br/>
      <b>Inicio:</b> ${fmt(start)} &nbsp;|&nbsp; <b>Fin:</b> ${fmt(end)}<br/>
    </div>`;
  }

  // ─── Getters de template ─────────────────────────────────────────────────

  get hasData(): boolean {
    return this.planningProcesses.length > 0 && !!this.selectedDate;
  }

  get isFirstDate(): boolean {
    return this.availableDates.indexOf(this.selectedDate) === 0;
  }

  get isLastDate(): boolean {
    return this.availableDates.indexOf(this.selectedDate) === this.availableDates.length - 1;
  }

  get hasActiveFilters(): boolean {
    return this.selectedWorkshopId !== '' || this.selectedOrderId !== '' || this.selectedDate !== '';
  }

  getTotalProducts(block: TallerChartBlock): number {
    return block.ordersOfTheDay.reduce((acc, item) => acc + item.cantidad, 0);
  }

  // ─── Scroll ─────────────────────────────────────────────────


  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  scrollOrders(container: HTMLElement, direction: 'left' | 'right'): void {
    const scrollAmount = 300;
    container.scrollBy({
      left: direction === 'right' ? scrollAmount : -scrollAmount,
      behavior: 'smooth'
    });
  }
  // Control de Fechas

  get ngbSelectedDate(): { year: number; month: number; day: number } {
  // Si por alguna razón no hay fecha, caemos en el día de hoy de forma segura para que no chille Ng-Bootstrap
  if (!this.selectedDate) {
    const today = new Date();
    return { year: today.getFullYear(), month: today.getMonth() + 1, day: today.getDate() };
  }
  const [year, month, day] = this.selectedDate.split('-').map(Number);
  return { year, month, day };
}

  get isToday(): boolean {
    return this.selectedDate === new Date().toISOString().split('T')[0];
  }

  // Arrow function obligatoria: ng-bootstrap llama esto sin contexto de clase
  isDateDisabled = (date: NgbDateStruct): boolean => {
    const pad = (n: number) => String(n).padStart(2, '0');
    return !this.availableDates.includes(
      `${date.year}-${pad(date.month)}-${pad(date.day)}`
    );
  };

  onCalendarDateSelect(date: NgbDate, drop: any): void {
    const pad = (n: number) => String(n).padStart(2, '0');
    const str = `${date.year}-${pad(date.month)}-${pad(date.day)}`;
    if (!this.availableDates.includes(str)) return;
    this.selectedDate = str;
    drop.close();
    this.renderingCharts = true;
    this.computeWorkshopBlocks();
    this.renderAllCharts();
  }

  get selectedDateIndex(): number {
    if (!this.selectedDate || this.availableDates.length === 0) return 0;
    return this.availableDates.indexOf(this.selectedDate) + 1;
  }

  setToday(): void {
    const todayStr = new Date().toISOString().split('T')[0];

    if (this.availableDates.includes(todayStr)) {
      this.selectedDate = todayStr;
    } else {
      this.toastr.info('No se encontraron órdenes de producción planificadas para la jornada de hoy.', 'Dashboard');
      return;
    }

    this.renderingCharts = true;
    this.computeWorkshopBlocks();
    this.renderAllCharts();
  }

  // Lógica de turnos
  getShiftForWorkshop(workshopCode: string): string {
    return this.selectedShifts[workshopCode] || 'all';
  }

  setShift(workshopCode: string, shift: string): void {
    if (this.getShiftForWorkshop(workshopCode) === shift) return;
    this.selectedShifts[workshopCode] = shift;

    this.renderingCharts = true;
    this.renderAllCharts(); // Fuerza el redibujado instantáneo
  }
}


