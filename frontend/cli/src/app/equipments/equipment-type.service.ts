import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { EquipmentType } from './equipment-type';

@Injectable({
  providedIn: 'root'
})
export class EquipmentTypeService {


  private equipmentTypesUrl = "rest/equipment-types";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.equipmentTypesUrl);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.equipmentTypesUrl}/id/${id}`);
  }

  save(equipmentType: EquipmentType): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.equipmentTypesUrl, equipmentType);
  }


}
