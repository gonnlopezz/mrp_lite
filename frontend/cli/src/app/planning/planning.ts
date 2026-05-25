import { Period } from './period';
import { Task } from '../products/task';
import { Equipment } from '../equipments/equipment';
import { manufacturingOrder } from '../orders/manufacturingOrder';

export interface Planning {
  id: number;
  period: Period;
  task: Task;
  equipment: Equipment;
}

export interface PlanningProcess {
  id: number;
  start: string;
  endDate: string;
  order: manufacturingOrder;
  plannings: Planning[];
}