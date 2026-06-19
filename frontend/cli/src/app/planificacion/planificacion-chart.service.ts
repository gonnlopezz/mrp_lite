import { Injectable } from '@angular/core';
import { ChartRow, TallerChartBlock } from './planificacion-dashboard';

declare var google: any;

@Injectable()
export class PlanificacionChartService {

  drawBlock(
    block: TallerChartBlock,
    container: HTMLElement,
    dayTimeRange: { min: Date; max: Date } | null,
    shiftRange: { min: Date; max: Date } | null = null
  ): void {
    if (block.rows.length === 0) {
      container.innerHTML = `
      <div class="text-muted small text-center py-3">
        No hay tareas planificadas para este taller en esta fecha.
      </div>`;
      return;
    }

    let filteredRows = block.rows;
    if (shiftRange) {
      filteredRows = block.rows.filter(row => {
        return row.start < shiftRange.max && row.end > shiftRange.min;
      });
    }

    if (filteredRows.length === 0) {
      container.innerHTML = `
      <div class="text-muted small text-center py-4 bg-light rounded-3 border border-dashed">
        <i class="fas fa-moon me-1 text-muted"></i> No hay tareas programadas para este turno.
      </div>`;
      return;
    }

    const { dataTable, colors } = this.buildDataTable(filteredRows);
    const options = this.buildChartOptions(filteredRows, colors, dayTimeRange, shiftRange);
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

    const sortedRows = [...rows].sort((a, b) => {
      const cmpEquip = a.equipmentCode.localeCompare(b.equipmentCode);
      if (cmpEquip !== 0) return cmpEquip;
      return a.start.getTime() - b.start.getTime();
    });

    const labelColorMap = new Map<string, string>();

    sortedRows.forEach(row => {
      dataTable.addRow([
        row.equipmentCode,
        row.rowLabel,
        row.tooltip,
        row.start,
        row.end
      ]);
      if (!labelColorMap.has(row.rowLabel)) {
        labelColorMap.set(row.rowLabel, row.color);
      }
    });

    return { dataTable, colors: Array.from(labelColorMap.values()) };
  }

  private buildChartOptions(
    rows: ChartRow[],
    colors: string[],
    dayTimeRange: { min: Date; max: Date } | null,
    shiftRange: { min: Date; max: Date } | null
  ): object {
    const uniqueEquipments = new Set(rows.map(r => r.equipmentCode)).size;
    const height = Math.max(120, uniqueEquipments * 41 + 50);

    let hAxisOptions: any = { format: 'HH:mm' };

    if (shiftRange) {
      hAxisOptions = {
        minValue: shiftRange.min,
        maxValue: shiftRange.max,
        format: 'HH:mm',
        gridlines: { count: 9 }
      };
    } else if (dayTimeRange) {
      hAxisOptions = {
        minValue: dayTimeRange.min,
        maxValue: dayTimeRange.max,
        format: 'HH:mm'
      };
    }

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
      animation: { duration: 0 },
      hAxis: hAxisOptions
    };
  }
}