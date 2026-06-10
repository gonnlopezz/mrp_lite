import { Equipo } from "../equipments/equipment";

export interface Taller {
    id: number;
    codigo: string;
    nombre: string;
    equipos: Equipo[];  
}