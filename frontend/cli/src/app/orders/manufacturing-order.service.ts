import { Injectable } from '@angular/core';
import { DataPackage } from '../data-package';
import { Observable } from 'rxjs/internal/Observable';
import { HttpClient } from '@angular/common/http';
import { manufacturingOrder } from './manufacturingOrder';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private ordersUrl = "rest/orders";

  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.ordersUrl);
  }

  allPlanned(): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.ordersUrl}/planned`);
  }

  byPage(page: number, size: number, state?: string): Observable<DataPackage> {
    if (state) return this.http.get<DataPackage>(`${this.ordersUrl}/page?page=${page - 1}&size=${size}&state=${state}`);
    return this.http.get<DataPackage>(`${this.ordersUrl}/page?page=${page - 1}&size=${size}`);
  }

  search(searchTerm: string, page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.ordersUrl}/search/${searchTerm}?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.ordersUrl}/id/${id}`);
  }



  getPlannings(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.ordersUrl}/id/${id}/plannings`);
  }

  save(workshop: manufacturingOrder): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.ordersUrl, workshop);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.ordersUrl}/id/${id}`);
  }
}
