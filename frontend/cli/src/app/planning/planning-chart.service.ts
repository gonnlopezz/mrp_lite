import { Injectable } from '@angular/core';
import { ChartRow, WorkshopChartBlock } from './planning-dashboard';

declare var google: any;

@Injectable()
export class PlanningChartService {

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

    // 1. 🔥 FILTRADO CRÍTICO: Si hay un turno seleccionado, filtramos las filas
    // para dejarle a Google solo lo que intersecta con las 8 horas del turno.
    let filteredRows = block.rows;
    if (shiftRange) {
      filteredRows = block.rows.filter(row => {
        // Una tarea pertenece al turno si empieza antes del fin del turno y termina después del inicio del turno
        return row.inicio < shiftRange.max && row.end > shiftRange.min;
      });
    }

    // Si el turno justo quedó vacío, mostramos el aviso en vez de un gráfico vacío
    if (filteredRows.length === 0) {
      container.innerHTML = `
      <div class="text-muted small text-center py-4 bg-light rounded-3 border border-dashed">
        <i class="fas fa-moon me-1 text-muted"></i> No hay tareas programadas para este turno.
      </div>`;
      return;
    }

    // 2. Construimos el gráfico usando únicamente las filas que pasaron el filtro
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

    const colorOrder: string[] = [];
    const seenLabels = new Set<string>();

    rows.forEach(row => {
      dataTable.addRow([
        row.equipoCode,
        { v: row.rowLabel, f: '' },
        row.tooltip,
        row.inicio,
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
    dayTimeRange: { min: Date; max: Date } | null,
    shiftRange: { min: Date; max: Date } | null // 📑 Recibimos el filtro del turno
  ): object {
    const uniqueEquipments = new Set(rows.map(r => r.equipoCode)).size;
    const height = Math.max(120, uniqueEquipments * 41 + 50);

    // 💡 Lógica de renderizado del eje temporal (hAxis)
    let hAxisOptions: any = { format: 'HH:mm' };

    if (shiftRange) {
      // Si el operario seleccionó un turno específico (Zoom Activo)
      hAxisOptions = {
        minValue: shiftRange.min,
        maxValue: shiftRange.max,
        format: 'HH:mm',
        gridlines: { count: 9 } // Divide las 8 horas exactas del turno en bloques de 1 hora
      };
    } else if (dayTimeRange) {
      // Fallback a la vista completa del día por defecto si existen datos
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
      hAxis: hAxisOptions // Inyección dinámica del viewport temporal
    };
  }
}