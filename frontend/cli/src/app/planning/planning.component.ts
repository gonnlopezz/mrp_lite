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

    plannings: PlanningProcess[] = [];
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
                this.plannings = <PlanningProcess[]> dataPackage.data;

                if (this.plannings && this.plannings.length > 0) {
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
        if (!this.plannings || this.plannings.length === 0 || !this.chartDiv) return;

        const options = {
            height: 800,
            timeline: {
                showRowLabels: true,
                groupByRowLabel: true,
                barLabelStyle: { fontSize: 10 }
            },
            hAxis: {
                format: 'HH:mm',
                gridlines: {
                    color: '#e0e0e0',
                    units: {
                        minutes: { format: ['HH:mm'] },
                        hours: { format: ['HH:mm'] }
                    }
                }
            }
        };

        const dataTable = new google.visualization.DataTable();
        dataTable.addColumn({ type: 'string', id: 'Equipo' });
        dataTable.addColumn({ type: 'string', id: 'Tarea' });
        dataTable.addColumn({ type: 'date', id: 'Inicio' });
        dataTable.addColumn({ type: 'date', id: 'Fin' });

        const rows: any[] = [];

        this.plannings.forEach((process) => {
            process.plannings.forEach((planning) => {
                try {
                    const equipmentCode = planning.equipment?.code || 'S/E';
                    const taskName = planning.task?.name || 'Tarea';

                    const start = new Date(planning.period.start);
                    const end = new Date(planning.period.endDate);

                    if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
                        rows.push([equipmentCode, taskName, start, end]);
                    }
                } catch (e) {
                    console.error('Error procesando tarea:', e);
                }
            });
        });

        if (rows.length > 0) {
            dataTable.addRows(rows);
            const chart = new google.visualization.Timeline(this.chartDiv.nativeElement);
            chart.draw(dataTable, options);
        }
    }

}
