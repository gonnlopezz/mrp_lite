
export interface Equipo {
    id: number;
    código: string;
    capacidad: number;
    tipo: {
        id?: number;
        nombre: string;       
    };
}