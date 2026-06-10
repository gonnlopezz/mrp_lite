
export interface Equipo {
    id: number;
    codigo: string;
    capacidad: number;
    tipo: {
        id?: number;
        nombre: string;       
    };
}