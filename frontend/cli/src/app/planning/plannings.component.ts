import { Component, OnInit, ViewChild, ElementRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Planning, PlanningProcess } from '../planning/planning';
import { WorkshopService } from '../workshops/workshop.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Workshop } from '../workshops/workshop';
import { Equipment } from '../equipments/equipment';
declare var google: any;

@Component({
    selector: 'app-planning',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './plannings.html',
})

export class PlanningComponent implements OnInit {
    @ViewChild('chartDiv') chartDiv!: ElementRef;

    @Input() title: string = '';
    planningProcesses: PlanningProcess[] = [];
    loading = true;
    workshop!: Workshop;

    constructor(
        private workshopService: WorkshopService,
        private route: ActivatedRoute
    ) { }

    ngOnInit(): void {
        this.getWorkshop();
        google.charts.load('current', { packages: ['timeline'] });

        google.charts.setOnLoadCallback(() => {
            this.loadPlanning();
        });
    }

    loadPlanning(): void {
        const id = this.route.snapshot.paramMap.get('id')!;
        this.workshopService.getPlannings(id).subscribe(
            dataPackage => {
                this.planningProcesses = <PlanningProcess[]>dataPackage.data;

                if (this.planningProcesses && this.planningProcesses.length > 0) {
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

        this.planningProcesses.forEach((process, processIndex) => {
            const color = processPalette[processIndex % processPalette.length];
            colors.push(color);

            const processLabel = `Proceso #${process.id}`;

            process.plannings.forEach(planning => {
                const start = new Date(planning.period.start);
                const end = new Date(planning.period.endDate);

                if (isNaN(start.getTime()) || isNaN(end.getTime())) return;

                const taskName = planning.task?.name || 'Tarea';
                const equipCode = planning.equipment?.code || 'S/E';

                const tooltip = `
                    <div style="padding:12px; font-size:13px;">
                        <b>${taskName}</b><br/>
                        Equipo: ${equipCode}<br/>
                        <b>Duración</b>: ${planning.task.duration} min<br/>
                    </div>`;

                rows.push([equipCode, { v: processLabel, f: taskName }, tooltip, start, end]);
            });
        });

        dataTable.addRows(rows);

        const totalPlannings = this.planningProcesses.reduce(
            (acc, p) => acc + p.plannings.length, 0
        );

        const options = {
            height: Math.max(400, totalPlannings * 45),
            width: '100%',                    // ← ancho completo
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

    getWorkshop(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id === 'new' || !id) {
            this.workshop = <Workshop>{ code: "", name: "", equipments: <Equipment[]>[] };
        } else {
            this.workshopService.get(id).subscribe(dataPackage => {
                this.workshop = <Workshop>dataPackage.data;
            });
        }
    }
}