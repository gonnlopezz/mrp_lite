import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { Customer } from './customer';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {


  private customersUrl = "rest/customers";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.customersUrl);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.customersUrl}/id/${id}`);
  }

  save(customer: Customer): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.customersUrl, customer);
  }


}
