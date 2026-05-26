export type ColorMode = 'by-process' | 'by-order';

export interface DayOrderSummary {
  orderId: number;
  orderLabel: string;
  customerName: string;
  productName: string;
  processCount: number;
  workshopNames: string[]; 
}

export interface ChartRow {
  equipmentCode: string;
  rowLabel: string;  
  tooltip: string;
  start: Date;
  end: Date;
  color: string;
}

export interface WorkshopChartBlock {
  workshopName: string;
  workshopCode: string;
  rows: ChartRow[];
}