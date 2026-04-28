import { Task } from "./task";

export interface Product {
    id: number;
    name: string;
    tasks: Task[];
}