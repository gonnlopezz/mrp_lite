import { Cliente } from "../clientes/cliente";
import { Producto } from "../productos/producto";

export interface PedidoFabricacion {
    id: number;
    fechaPedido: string | Date;    
    fechaEntrega: string | Date; 
    cantidad: number;
    estado: EstadoPedido;
    cliente: Cliente;
    producto: Producto;
}

export enum EstadoPedido {
    PENDIENTE,
    PLANIFICADO,
    NO_PLANIFICABLE,
    FINALIZADO
    }