import { Component, OnInit, ViewChild, ElementRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Planning, PlanningProcess } from '../planning/planning';
import { WorkshopService } from '../workshops/workshop.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Taller } from '../workshops/workshop';
import { Equipo } from '../equipments/equipment';
import { PedidoFabricacion } from '../orders/manufacturingOrder';
import { OrderService } from '../orders/manufacturing-order.service';
declare var google: any;

@Component({
    selector: 'app-planning',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './plannings.html',
})
export class PlanningComponent implements OnInit {
    @ViewChild('chartDiv') chartDiv!: ElementRef;
    private chart: any;

    planningProcesses: PlanificacionProcess[] = [];
    order?: PedidoFabricacion;
    workshop?: Taller;
    loading = true;
    availableDates: string[] = []; // Guardará formatos "2026-05-22"
    selectedDate: string = '';

    private chartReady = false;
    private dataReady = false;

    isOrderContext = false;
    entityName = '';
    entityCode = '';

    constructor(
        private workshopService: TallerService,
        private orderService: OrderService,
        private route: ActivatedRoute
    ) { }

    

    ngOnInit(): void {
        this.isOrderContext = this.route.snapshot.url[0].path.includes('orders');

        this.loadEntityDetails();

        google.charts.load('current', { packages: ['timeline'] });
        google.charts.setOnLoadCallback(() => {
            this.loadPlanning();
        });
    }

    ngAfterViewInit() {
        this.chartReady = true;
        if (this.dataReady) this.generateTimelineChart();
    }

    loadEntityDetails(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (!id || id === 'new') return;

        if (this.isOrderContext) {
            this.orderService.get(id).subscribe((dataPackage: any) => {
                const order = <PedidoFabricacion>dataPackage.data;
                this.order = order;
                this.entityName = `Pedido de ${order.cliente?.razónSocial}`;
                this.entityCode = `Orden #${order.id}`;
            });
        } else {
            this.workshopService.get(id).subscribe((dataPackage: any) => {
                const workshop = <Taller>dataPackage.data;
                this.entityName = workshop.nombre;
                this.entityCode = workshop.código;
            });
        }
    }

    loadPlanning(): void {
        const id = this.route.snapshot.paramMap.get('id')!;

        const request$ = this.isOrderContext
            ? this.orderService.getPlannings(id)
            : this.workshopService.getPlannings(id);

        request$.subscribe({
            next: (dataPackage: any) => {
                this.planningProcesses = <PlanningProcess[]>dataPackage.data;

                // Extraemos las fechas disponibles para el filtro// 1. EXTRAER FECHAS PRIMERO
                const dates = new Set<string>();
                this.planningProcesses.forEach(p => p.planificaciones.forEach(pl => {
                    dates.add(pl.periodo.inicio.split('T')[0]);
                }));
                this.availableDates = Array.from(dates).sort();

                // 2. SETEAR LA FECHA POR DEFECTO SI ESTÁ VACÍA
                if (this.availableDates.length > 0 && !this.selectedDate) {
                    this.selectedDate = this.availableDates[0];
                }

                // 3. SOLO DIBUJAR SI YA TENEMOS UNA FECHA SELECCIONADA
                if (this.selectedDate) {
                    setTimeout(() => this.generateTimelineChart(), 100);
                }

                this.loading = false;
            },
            error: (error: any) => {
                console.error('Error al cargar planificación:', error);
                this.loading = false;
            }
        });
    }
    generateTimelineChart(): void {
        if (!this.planningProcesses || this.planningProcesses.length === 0 || !this.chartDiv) return;

        const processPalette = [
            '#3366cc', '#dc3912', '#ff9900', '#109618',
            '#990099', '#0099c6', '#dd4477', '#66aa00'
        ];

        const dataTable = new google.visualization.DataTable();
        dataTable.addColumn({ type: 'string', id: 'Equipo' });
        dataTable.addColumn({ type: 'string', id: 'Proceso' });
        dataTable.addColumn({ type: 'string', role: 'tooltip', p: { html: true } });
        dataTable.addColumn({ type: 'date', id: 'Inicio' });
        dataTable.addColumn({ type: 'date', id: 'Fin' });

        const rows: any[] = [];
        const colors: string[] = [];
        const uniqueEquipments = new Set<string>(); // Para contar cuántas filas reales habrá

        this.planningProcesses.forEach((process, processIndex) => {
            const color = processPalette[processIndex % processPalette.length];
            colors.push(color);

            const processLabel = `Proceso #${process.id}`;

            process.planificaciones.forEach(planning => {
                const start = new Date(planning.periodo.inicio);
                const end = new Date(planning.periodo.fin);

                const startDateStr = planning.periodo.inicio.split('T')[0];

                if (startDateStr !== this.selectedDate) return;

                if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

                const taskName = planning.tarea?.name || 'Tarea';
                const equipCode = planning.equipo?.código || 'S/E';

                uniqueEquipments.add(equipCode);

                const tooltip = `
                    <div style="padding:12px; font-size:13px;">
                        <b style="color: ${color};">${processLabel}</b><br/>
                        <b>Tarea:</b> ${taskName}<br/>
                        <b>Equipo:</b> ${equipCode}<br/>
                        <b>Duración:</b> ${planning.tarea.tiempo} min<br/>
                        <b>Inicio:</b> ${start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}<br/>
                    </div>`;

                // Dejamos el formato 'f' vacío para que las barras queden de colores limpios
                rows.push([equipCode, { v: processLabel, f: '' }, tooltip, start, end]);
            });
        });

        dataTable.addRows(rows);

        const calculatedHeight = Math.max(150, (uniqueEquipments.size * 50) + 50);

        const options = {
            height: calculatedHeight,
            colors,
            tooltip: { isHtml: true },
            timeline: {
                showRowLabels: true,
                groupByRowLabel: true,
                colorByRowLabel: false,
                rowLabelStyle: { fontSize: 13, color: '#475569' }
            },
        };
        if (this.chart) {
            this.chart.clearChart();
        }
        this.chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
        this.chart.draw(dataTable, options);
    }

    changeDate(direction: 'prev' | 'next'): void {
        const currentIndex = this.availableDates.indexOf(this.selectedDate);

        if (direction === 'prev' && currentIndex > 0) {
            this.selectedDate = this.availableDates[currentIndex - 1];
        } else if (direction === 'next' && currentIndex < this.availableDates.length - 1) {
            this.selectedDate = this.availableDates[currentIndex + 1];
        }

        this.generateTimelineChart();
    }

    isFirstDate(): boolean {
        return this.availableDates.indexOf(this.selectedDate) === 0;
    }

    isLastDate(): boolean {
        return this.availableDates.indexOf(this.selectedDate) === this.availableDates.length - 1;
    }

    ngOnDestroy(): void {
        if (this.chart) this.chart.clearChart();
    }

}