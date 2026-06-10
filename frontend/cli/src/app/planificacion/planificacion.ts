import { Periodo } from './periodo';
import { Tarea } from '../productos/tarea';
import { Equipo } from '../equipos/equipo';
import { PedidoFabricacion } from '../pedidos/pedido';

export interface Planificacion {
  id: number;
  periodo: Periodo;
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