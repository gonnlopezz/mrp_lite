// planning-dashboard.component.ts
import {
  Component, OnInit, AfterViewInit,
  ViewChildren, QueryList, ElementRef, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { WorkshopService } from '../workshops/workshop.service';
import { OrderService } from '../orders/manufacturing-order.service';
import { PlanningService } from './planning.service';
import { PlanningChartService } from './planning-chart.service';   // <-- nuevo
import { Workshop } from '../workshops/workshop';
import { manufacturingOrder } from '../orders/manufacturingOrder';
import { Product } from '../products/product';
import { PlanningProcess } from '../planning/planning';
import { ColorMode, ChartRow, WorkshopChartBlock, DayOrderSummary, DayProductSummary } from './planning-dashboard';
import { productService } from '../products/product.service';
import { RouterModule } from '@angular/router';

const PROCESS_PALETTE = [
  '#3366cc', '#dc3912', '#ff9900', '#109618',
  '#990099', '#0099c6', '#dd4477', '#66aa00'
];

declare var google: any;

@Component({
  selector: 'app-planning-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './planning-dashboard.html',
  providers: [PlanningChartService]
})
export class PlanningDashboardComponent implements OnInit, AfterViewInit {

  @ViewChildren('chartDiv') chartDivs!: QueryList<ElementRef>;

  workshops: Workshop[] = [];
  orders: manufacturingOrder[] = [];
  planningProcesses: PlanningProcess[] = [];

  selectedWorkshopId: string = '';
  selectedOrderId: string = '';
  selectedDate: string = '';
  colorMode: ColorMode = 'by-process';

  availableDates: string[] = [];
  workshopBlocks: WorkshopChartBlock[] = [];
  dayTimeRange: { min: Date; max: Date } | null = null;

  loading: boolean = false;
  renderingCharts: boolean = false;
  googleChartsLoaded = false;

  private equipmentWorkshopMap = new Map<number, { code: string; name: string }>();
  private taskProductMap = new Map<number, string>();

  constructor(
    private workshopService: WorkshopService,
    private orderService: OrderService,
    private productService: productService,
    private planningService: PlanningService,
    private chartService: PlanningChartService,
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
      orders: this.orderService.all(),
      products: this.productService.all()
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

  // ─── Fetch principal ─────────────────────────────────────────────────────

  onFiltersChange(): void {
    this.selectedDate = '';
    this.fetchAndRender();
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
    this.onFiltersChange();
  }

  // ─── Renderizado ─────────────────────────────────────────────────────────

  renderAllCharts(): void {
    if (!this.googleChartsLoaded || !this.hasData) return;
    this.cdr.detectChanges();
    // La suscripción en ngAfterViewInit se encarga del dibujo
  }

  private drawCharts(divs: ElementRef[]): void {
    this.workshopBlocks.forEach((block, index) => {
      const divRef = divs[index];
      if (!divRef) return;
      this.chartService.drawBlock(block, divRef.nativeElement, this.dayTimeRange);
    });
    this.renderingCharts = false;
    this.cdr.detectChanges();
  }

  // ─── Mapas de lookup ─────────────────────────────────────────────────────

  private buildEquipmentWorkshopMap(): void {
    this.equipmentWorkshopMap.clear();
    this.workshops.forEach(workshop =>
      workshop.equipments?.forEach(equipment =>
        this.equipmentWorkshopMap.set(equipment.id, {
          code: workshop.code,
          name: workshop.name
        })
      )
    );
  }

  private buildTaskProductMap(products: Product[]): void {
    this.taskProductMap.clear();
    products.forEach(product =>
      product.tasks?.forEach(task =>
        this.taskProductMap.set(task.id, product.name)
      )
    );
  }

  // ─── Compute ─────────────────────────────────────────────────────────────

  private computeAvailableDates(): void {
    const dates = new Set<string>();
    this.planningProcesses.forEach(process =>
      process.plannings?.forEach(planning => {
        const dateStr = planning.period?.start?.split('T')[0];
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

      process.plannings?.forEach(planning => {
        const dateStr = planning.period?.start?.split('T')[0];
        if (dateStr !== this.selectedDate) return;

        const start = new Date(planning.period.start);
        const end = new Date(planning.period.endDate);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

        const workshop = this.equipmentWorkshopMap.get(planning.equipment?.id);
        if (!workshop) return;

        const row: ChartRow = {
          equipmentCode: planning.equipment?.code ?? 'S/E',
          rowLabel: processLabel,
          tooltip: this.buildTooltip(
            processLabel,
            planning.task?.name ?? 'Tarea',
            planning.equipment?.code ?? 'S/E',
            start, end,  
            color
          ),
          start, end, color
        };

        if (!blocksMap.has(workshop.code)) {
          blocksMap.set(workshop.code, {
            workshopName: workshop.name, workshopCode: workshop.code,
            rows: [], ordersOfTheDay: [], productsOfTheDay: []
          });
        }
        blocksMap.get(workshop.code)!.rows.push(row);
      });
    });

    blocksMap.forEach((block, workshopCode) => {
      const { orders, products } = this.computeSummaryForWorkshop(workshopCode);
      block.ordersOfTheDay = orders;
      block.productsOfTheDay = products;
    });

    this.workshopBlocks = Array.from(blocksMap.values());
    this.computeDayTimeRange();
  }

  private computeSummaryForWorkshop(workshopCode: string): {
    orders: DayOrderSummary[]; products: DayProductSummary[];
  } {
    const ordersMap = new Map<number, DayOrderSummary>();
    const productsMap = new Map<string, DayProductSummary>();

    this.planningProcesses.forEach(process => {
      const activeHere = process.plannings?.filter(p => {
        const dateStr = p.period?.start?.split('T')[0];
        const workshop = this.equipmentWorkshopMap.get(p.equipment?.id);
        return dateStr === this.selectedDate && workshop?.code === workshopCode;
      }) ?? [];

      if (activeHere.length === 0) return;

      if (process.order?.id) {
        const order = process.order;
        if (!ordersMap.has(order.id)) {
          ordersMap.set(order.id, {
            orderId: order.id,
            orderLabel: `Orden #${order.id}`,
            customerName: order.customer?.companyName ?? 'Cliente',
            productName: order.product?.name ?? 'Sin producto',
            quantity: order.quantity ?? 0,
            processCount: 1,
            workshopNames: [workshopCode]
          });
        } else {
          ordersMap.get(order.id)!.processCount++;
        }
      } else {
        const taskId = activeHere[0].task?.id;
        const productName = taskId ? this.taskProductMap.get(taskId) : null;
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

  private computeDayTimeRange(): void {
    const allDates = this.workshopBlocks.flatMap(b =>
      b.rows.flatMap(r => [r.start.getTime(), r.end.getTime()])
    );
    if (allDates.length === 0) { this.dayTimeRange = null; return; }
    this.dayTimeRange = {
      min: new Date(Math.min(...allDates)),
      max: new Date(Math.max(...allDates))
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

  private colorKey(process: PlanningProcess): string {
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
    color: string
  ): string {
    const fmt = (d: Date) => d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const durationMinutes = Math.round((end.getTime() - start.getTime()) / 60000);

    return `
    <div style="padding:10px; font-size:13px; min-width:180px; font-family:sans-serif;">
      <b style="color:${color};">${processLabel}</b><br/>
      <b>Tarea:</b> ${taskName}<br/>
      <b>Equipo:</b> ${equipCode}<br/>
      <b>Duración:</b> ${durationMinutes} min<br/>
      <b>Inicio:</b> ${fmt(start)}<br/>
      <b>Fin:</b> ${fmt(end)}<br/>
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
    return this.selectedWorkshopId !== '' || this.selectedOrderId !== '';
  }

  getTotalProducts(block: WorkshopChartBlock): number {
  return block.ordersOfTheDay.reduce((acc, item) => acc + item.quantity, 0);
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
}