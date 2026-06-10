import { Equipo } from "../equipos/equipo";

export interface Taller {
    id: number;
    codigo: string;
    nombre: string;
    equipos: Equipo[];  
}