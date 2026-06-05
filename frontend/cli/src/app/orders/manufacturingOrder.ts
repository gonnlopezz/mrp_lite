import { Customer } from "../customers/customer";
import { Product } from "../products/product";

export interface manufacturingOrder {
    id: number;
    orderDate: string | Date;    
    deliveryDate: string | Date; 
    quantity: number;
    state: orderState;
    customer: Customer;
    product: Product;
}

export enum orderState {
    PENDIENTE,
    PLANIFICADO,
    NO_PLANIFICABLE,
    FINALIZADO
    }