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

    this.planningsProcess.forEach((process, pIndex) => {
        const processLabel = `Planificación: ${process.id.toString()}` || `Proceso ${pIndex + 1}`;

        process.plannings.forEach((planning) => {
            const start = new Date(planning.period.start);
            const end   = new Date(planning.period.endDate);
            if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

            const taskName    = planning.task?.name    || 'Tarea';
            const equipCode   = planning.equipment?.code || 'S/E';

            const tooltip = `
                <div style="padding:12px; font-size:13px;">
                    <b>${taskName}</b><br/>
                    Equipo:${equipCode}<br/>
                    <b>Duración</b>: ${planning.task.duration} min<br/>
                </div>`;

            rows.push([
                equipCode,
                {v: processLabel, f:taskName},
                tooltip,
                start,
                end
            ]);
        });
    });

    dataTable.addRows(rows);

    const seenLabels: string[] = [];
    const colors: string[] = [];

    this.planningsProcess.forEach((process, pIndex) => {
        const label = `Planificación: ${process.id.toString()}` || `Proceso ${pIndex + 1}`;
        if (!seenLabels.includes(label)) {
            seenLabels.push(label);
            colors.push(processPalette[pIndex % processPalette.length]);
        }
    });

    const options = {
        height: Math.max(400, this.planningsProcess.reduce(
            (acc, p) => acc + p.plannings.length, 0) * 45
        ),
        colors,
        tooltip: { isHtml: true },
        timeline: {
            showRowLabels: true,
            groupByRowLabel: true,
            colorByRowLabel: false,
        },
    };

    const chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
    chart.draw(dataTable, options);
}


}
