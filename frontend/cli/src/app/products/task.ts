
export interface Task {
    id: number;
    name: string;
    duration: number;
    type: {
        id?: number;
        name: string;
    } 
}