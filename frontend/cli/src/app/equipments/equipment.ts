import { EquipmentType } from "./equipment-type";

export interface Equipment {
    id: number;
    code: string;
    capacity: number;
    type: {
        id?: number;
        name: string;       
    };
}