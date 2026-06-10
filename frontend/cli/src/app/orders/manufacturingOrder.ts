import { Cliente } from "../customers/customer";
import { Producto } from "../products/product";

export interface PedidoFabricacion {
    id: number;
    fechaPedido: string | Date;    
    fechaEntrega: string | Date; 
    cantidad: number;
    estado: EstadoPedido;
    cliente: Cliente;
    producto: Productoo;
}

export enum EstadoPedido {
    PENDIENTE,
    PLANIFICADO,
    NO_PLANIFICABLE,
    FINALIZADO
    }