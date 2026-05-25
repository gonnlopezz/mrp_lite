import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DataPackage } from '../data-package';
import { PlanningProcess } from './planning';

@Injectable({
  providedIn: 'root'
})
export class PlanningService {

  private planningUrl = "rest/plannings";

  constructor(private http: HttpClient) { }

  getAll(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.planningUrl);
  }

  getById(id: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.planningUrl}/id/${id}`);
  }

  save(request: any): Observable<DataPackage> {
    return this.http.post<DataPackage>(`${this.planningUrl}/order`, request);
  }

  getByPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.planningUrl}/page?page=${page - 1}&size=${size}`);
  }

  getPlanningsFiltered(workshopId: string | number, orderId: string | number): Observable<DataPackage> {
    let params = '?';
    if (workshopId) params += `workshopId=${workshopId}&`;
    if (orderId) params += `orderId=${orderId}`;

    if (params.endsWith('&') || params.endsWith('?')) {
      params = params.slice(0, -1);
    }

    return this.http.get<DataPackage>(`${this.planningUrl}/filtered${params}`);
  }

}