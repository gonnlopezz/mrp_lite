import { Equipment } from "../equipments/equipment";

export interface Workshop {
    id: number;
    code: string;
    name: string;
    equipments: Equipment[];  
}