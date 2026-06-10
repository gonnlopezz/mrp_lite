import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DataPackage } from '../data-package';
import { Taller } from './workshop';

@Injectable({
  providedIn: 'root'
})
export class WorkshopService {

  private workshopsUrl = "rest/workshops";

  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.workshopsUrl);
  }

  byPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.workshopsUrl}/page?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.workshopsUrl}/id/${id}`);
  }

  getPlannings(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.workshopsUrl}/id/${id}/plannings`);
  }

  save(taller: Taller): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.workshopsUrl, taller);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.workshopsUrl}/id/${id}`);
  }

  search(searchTerm: string, page: number = 1, size: number = 6): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.workshopsUrl}/search/${searchTerm}?page=${page - 1}&size=${size}`);
  }
}
