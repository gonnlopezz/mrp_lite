import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { Cliente } from './cliente';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {


  private clientesUrl = "rest/customers";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.clientesUrl);
  }

  byPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.clientesUrl}/page?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.clientesUrl}/id/${id}`);
  }

  save(cliente: Cliente): Observable<DataPackage> {
    return cliente.id?
    this.http.put<DataPackage>(this.clientesUrl, cliente) :
    this.http.post<DataPackage>(this.clientesUrl, cliente);
  }
  
  delete(id: number): Observable<DataPackage> {   
    return this.http.delete<DataPackage>(`${this.clientesUrl}/id/${id}`);
  }

  search(searchTerm: string, page: number = 1, size: number = 10): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.clientesUrl}/search/${searchTerm}?page=${page - 1}&size=${size}`);
  }

}
