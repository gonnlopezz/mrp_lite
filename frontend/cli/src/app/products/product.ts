import { Tarea } from "./task";

export interface Producto {
    id: number;
    nombre: string;
    tareas: Tarea[];
}