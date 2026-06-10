export type ColorMode = 'by-process' | 'by-order';

export interface DayOrderSummary {
  orderId:       number;
  orderLabel:    string;
  customerName:  string;
  productName:   string;
  cantidad:      number;
  processCount:  number;
  workshopNames: string[];
}

export interface DayProductSummary {
  productName:  string;
  processCount: number;
}

export interface ChartRow {
  equipmentCode: string;
  rowLabel:      string;
  tooltip:       string;
  start:         Date;
  end:           Date;
  color:         string;
}

export interface WorkshopChartBlock {
  workshopName:    string;
  workshopCode:    string;
  rows:            ChartRow[];
  ordersOfTheDay:  DayOrderSummary[];
  productsOfTheDay: DayProductSummary[];
}