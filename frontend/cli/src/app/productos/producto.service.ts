import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { Producto } from './producto';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {


  private productsUrl = "rest/products";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.productsUrl);
  }

  byPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productsUrl}/page?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productsUrl}/id/${id}`);
  }

  save(producto: Producto): Observable<DataPackage> {
    return producto.id ?
      this.http.put<DataPackage>(this.productsUrl, producto) :
      this.http.post<DataPackage>(this.productsUrl, producto);
  }

  delete(id: number): Observable<DataPackage> {
    return this.http.delete<DataPackage>(`${this.productsUrl}/id/${id}`);
  }

  search(searchTerm: string, page: number = 1, size: number = 6): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productsUrl}/search/${searchTerm}?page=${page - 1}&size=${size}`);
  }

}
