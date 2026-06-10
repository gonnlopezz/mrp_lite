
export interface Tarea {
    id: number;
    nombre: string;
    tiempo: number;
    tipo: {
        id?: number;
        nombre: string;
    } 
}