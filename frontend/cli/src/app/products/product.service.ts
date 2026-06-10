import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { Producto } from './product';

@Injectable({
  providedIn: 'root'
})
export class productService {


  private productsUrl = "rest/products";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.productosUrl);
  }

  byPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productosUrl}/page?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productosUrl}/id/${id}`);
  }

  save(producto: Productoo): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.productosUrl, producto);
  }

  delete(id: number): Observable<DataPackage> {
    return this.http.delete<DataPackage>(`${this.productosUrl}/id/${id}`);
  }

  search(searchTerm: string, page: number = 1, size: number = 6): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.productosUrl}/search/${searchTerm}?page=${page - 1}&size=${size}`);
  }

}
