import { Periodo } from './period';
import { Tarea } from '../products/task';
import { Equipo } from '../equipments/equipment';
import { PedidoFabricacion } from '../orders/manufacturingOrder';

export interface Planificacion {
  id: number;
  periodo: Periodoo;
  tarea: Tarea;
  equipo: Equipo;
}

export interface ProcesoPlanificacion {
  id: number;
  inicio: string;
  fin: string;
  pedido: PedidoFabricacion;
  planificaciones: Planificacion[];
}