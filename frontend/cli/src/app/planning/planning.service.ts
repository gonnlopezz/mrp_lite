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
    return this.http.post<DataPackage>(this.planningUrl, request);
  }

  getByPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.planningUrl}/page?page=${page - 1}&size=${size}`);
  }
}