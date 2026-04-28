
export interface Task {
    id: number;
    code: string;
    name: string;
    type: {
        id?: number;
        name: string;
    } 
}