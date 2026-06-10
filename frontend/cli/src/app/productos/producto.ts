import { Tarea } from "./tarea";

export interface Producto {
    id: number;
    nombre: string;
    tareas: Tarea[];
}