import { Injectable } from '@angular/core';

const PALETTE: string[] = [
    '#4E79A7', '#F28E2B', '#E15759', '#76B7B2',
    '#59A14F', '#EDC948', '#B07AA1', '#FF9DA7',
    '#9C755F', '#BAB0AC', '#1B998B', '#D4A017',
    '#3A86C8', '#FF9F1C', '#2EC4B6', '#E71D36',
    '#9B5DE5', '#F15BB5', '#43AA8B', '#00BBF9',
    '#F9C74F', '#90BE6D', '#277DA1', '#577590'
];

@Injectable({ providedIn: 'root' })
export class ColorService {
    assign(keysInOrder: string[]): Map<string, string> {
        const map = new Map<string, string>();
        let nextIndex = 0;
        for (const key of keysInOrder) {
            if (!map.has(key)) {
                map.set(key, PALETTE[nextIndex % PALETTE.length]);
                nextIndex++;
            }
        }
        return map;
    }
}