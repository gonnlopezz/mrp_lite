import { Component, OnInit, ViewChild, ElementRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Planning, PlanningProcess } from '../planning/planning';
import { WorkshopService } from '../workshops/workshop.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Workshop } from '../workshops/workshop';
import { Equipment } from '../equipments/equipment';
import { manufacturingOrder } from '../orders/manufacturingOrder';
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

    planningProcesses: PlanningProcess[] = [];
    order?: manufacturingOrder;
    loading = true;
    availableDates: string[] = []; // Guardará formatos "2026-05-22"
    selectedDate: string = '';

    isOrderContext = false;
    entityName = '';
    entityCode = '';

    constructor(
        private workshopService: WorkshopService,
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

    loadEntityDetails(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (!id || id === 'new') return;

        if (this.isOrderContext) {
            this.orderService.get(id).subscribe((dataPackage: any) => {
                const order = <manufacturingOrder>dataPackage.data;
                this.order = order;
                this.entityName = `Pedido de ${order.customer?.companyName}`;
                this.entityCode = `Orden #${order.id}`;
            });
        } else {
            this.workshopService.get(id).subscribe((dataPackage: any) => {
                const workshop = <Workshop>dataPackage.data;
                this.entityName = workshop.name;
                this.entityCode = workshop.code;
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
                this.planningProcesses.forEach(p => p.plannings.forEach(pl => {
                    dates.add(pl.period.start.split('T')[0]);
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

            process.plannings.forEach(planning => {
                const start = new Date(planning.period.start);
                const end = new Date(planning.period.endDate);

                const startDateStr = planning.period.start.split('T')[0];

                // 💡 FILTRO AQUÍ: Solo agregamos si la fecha coincide
                if (startDateStr !== this.selectedDate) return;

                if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

                const taskName = planning.task?.name || 'Tarea';
                const equipCode = planning.equipment?.code || 'S/E';

                uniqueEquipments.add(equipCode);

                const tooltip = `
                    <div style="padding:12px; font-size:13px;">
                        <b style="color: ${color};">${processLabel}</b><br/>
                        <b>Tarea:</b> ${taskName}<br/>
                        <b>Equipo:</b> ${equipCode}<br/>
                        <b>Duración:</b> ${planning.task.duration} min<br/>
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
            // Eliminamos "width: '100%'" de acá para que respete el min-width del HTML
            colors,
            tooltip: { isHtml: true },
            timeline: {
                showRowLabels: true,
                groupByRowLabel: true,
                colorByRowLabel: false,
                rowLabelStyle: { fontSize: 13, color: '#475569' }
            },
        };

        const chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
        chart.draw(dataTable, options);
    }

    // Método para navegar
    changeDate(direction: 'prev' | 'next'): void {
        const currentIndex = this.availableDates.indexOf(this.selectedDate);

        if (direction === 'prev' && currentIndex > 0) {
            this.selectedDate = this.availableDates[currentIndex - 1];
        } else if (direction === 'next' && currentIndex < this.availableDates.length - 1) {
            this.selectedDate = this.availableDates[currentIndex + 1];
        }

        this.generateTimelineChart();
    }

    // Helpers para desactivar flechas si no hay más fechas
    isFirstDate(): boolean {
        return this.availableDates.indexOf(this.selectedDate) === 0;
    }

    isLastDate(): boolean {
        return this.availableDates.indexOf(this.selectedDate) === this.availableDates.length - 1;
    }

}