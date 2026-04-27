import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DataPackage } from '../data-package';
import { Workshop } from './workshop';

@Injectable({
  providedIn: 'root'
})
export class WorkshopService {

  private workshopsUrl = "rest/workshops";

  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.workshopsUrl);
  }

  get(id: string): Observable<DataPackage> {    
    return this.http.get<DataPackage>(`${this.workshopsUrl}/id/${id}`);
  }

  save(workshop: Workshop): Observable<DataPackage> { 
    return this.http.post<DataPackage>(this.workshopsUrl, workshop);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.workshopsUrl}/id/${id}`);
  }
}
