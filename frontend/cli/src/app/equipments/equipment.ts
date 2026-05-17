
export interface Equipment {
    id: number;
    code: string;
    capacity: number;
    type: {
        id?: number;
        name: string;       
    };
}