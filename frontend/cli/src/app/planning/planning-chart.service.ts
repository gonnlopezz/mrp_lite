
import { Injectable } from '@angular/core';
import { ChartRow, WorkshopChartBlock } from './planning-dashboard';

declare var google: any;

@Injectable()
export class PlanningChartService {

  drawBlock(
    block: WorkshopChartBlock,
    container: HTMLElement,
    dayTimeRange: { min: Date; max: Date } | null
  ): void {
    if (block.rows.length === 0) {
      container.innerHTML = `
        <div class="text-muted small text-center py-3">
          No hay tareas planificadas para este taller en esta fecha.
        </div>`;
      return;
    }

    const { dataTable, colors } = this.buildDataTable(block.rows);
    const options               = this.buildChartOptions(block.rows, colors, dayTimeRange);
    const chart                 = new google.visualization.Timeline(container);
    chart.draw(dataTable, options);
  }

  private buildDataTable(rows: ChartRow[]): { dataTable: any; colors: string[] } {
    const dataTable = new google.visualization.DataTable();
    dataTable.addColumn({ type: 'string', id: 'Equipo'  });
    dataTable.addColumn({ type: 'string', id: 'Proceso' });
    dataTable.addColumn({ type: 'string', role: 'tooltip', p: { html: true } });
    dataTable.addColumn({ type: 'date',   id: 'Inicio'  });
    dataTable.addColumn({ type: 'date',   id: 'Fin'     });

    const colorOrder: string[] = [];
    const seenLabels           = new Set<string>();

    rows.forEach(row => {
      dataTable.addRow([
        row.equipmentCode,
        { v: row.rowLabel, f: '' },
        row.tooltip,
        row.start,
        row.end
      ]);

      if (!seenLabels.has(row.rowLabel)) {
        seenLabels.add(row.rowLabel);
        colorOrder.push(row.color);
      }
    });

    return { dataTable, colors: colorOrder };
  }

  private buildChartOptions(
    rows: ChartRow[],
    colors: string[],
    dayTimeRange: { min: Date; max: Date } | null
  ): object {
    const uniqueEquipments = new Set(rows.map(r => r.equipmentCode)).size;
    const height           = Math.max(120, uniqueEquipments * 41 + 50);

    return {
      height,
      colors,
      tooltip: { isHtml: true },
      timeline: {
        showRowLabels:   true,
        groupByRowLabel: true,
        colorByRowLabel: false,
        rowLabelStyle:   { fontSize: 12, color: '#475569' },
        barLabelStyle:   { fontSize: 11 }
      },
      animation: { duration: 0 },
      ...(dayTimeRange && {
        hAxis: {
          minValue: dayTimeRange.min,
          maxValue: dayTimeRange.max
        }
      })
    };
  }
}