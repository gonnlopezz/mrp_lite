import { Customer } from "../customers/customer";
import { Product } from "../products/product";

export interface manufacturingOrder {
    id: number;
    orderDate: Date;
    deliveryDate: Date;
    quantity: number;
    customer: Customer;
    product: Product;
}