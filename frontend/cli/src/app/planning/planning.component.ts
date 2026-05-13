import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanningService } from './planning.service';
import { PlanningProcess } from './planning';

declare var google: any;

@Component({
    selector: 'app-planning',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './planning.html',
})
export class PlanningComponent implements OnInit {
    @ViewChild('chartDiv') chartDiv!: ElementRef;

    planningsProcess: PlanningProcess[] = [];
    loading = true;

    constructor(private planningService: PlanningService) { }

    ngOnInit(): void {
        google.charts.load('current', { packages: ['timeline'] });

        google.charts.setOnLoadCallback(() => {
            this.loadPlanning();
        });
    }

    loadPlanning(): void {
        this.planningService.getAll().subscribe(
            dataPackage => {
                this.planningsProcess = <PlanningProcess[]>dataPackage.data;

                if (this.planningsProcess && this.planningsProcess.length > 0) {
                    setTimeout(() => this.generateTimelineChart(), 100);
                }
                this.loading = false;
            },
            (error) => {
                console.error('Error al cargar planificación:', error);
                this.loading = false;
            }
        );
    }

generateTimelineChart(): void {
    if (!this.planningsProcess || this.planningsProcess.length === 0 || !this.chartDiv) return;

    const processPalette = ['#3366cc', '#dc3912', '#ff9900', '#109618', '#990099', '#0099c6'];
    const rows: any[] = [];

    // 1. Generamos las filas - cada planning obtiene el color de su PlanningProcess
    this.planningsProcess.forEach((process, pIndex) => {
        const pColor = processPalette[pIndex % processPalette.length];

        process.plannings.forEach((planning) => {
            const start = new Date(planning.period.start);
            const end = new Date(planning.period.endDate);

            if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
                rows.push([
                    planning.equipment?.code || 'S/E',
                    planning.task?.name || 'Tarea',
                    start,
                    end,
                    pColor // Color directo basado en el índice del PlanningProcess
                ]);
            }
        });
    });

    // 2. DataTable con columna de estilo
    const dataTable = new google.visualization.DataTable();
    dataTable.addColumn({ type: 'string', id: 'Equipo' });
    dataTable.addColumn({ type: 'string', id: 'Tarea' });
    dataTable.addColumn({ type: 'date', id: 'Inicio' });
    dataTable.addColumn({ type: 'date', id: 'Fin' });
    dataTable.addColumn({ type: 'string', role: 'style' });
    dataTable.addRows(rows);

    const options = {
        height: 600,
        timeline: {
            showRowLabels: true,
            groupByRowLabel: true,
            colorByRowLabel: false
        }
    };

    const chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
    chart.draw(dataTable, options);
}


}
