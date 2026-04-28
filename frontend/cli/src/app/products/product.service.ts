import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { Product } from './product';

@Injectable({
  providedIn: 'root'
})
export class productService {


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

  save(product: Product): Observable<DataPackage> {
    return this.http.post<DataPackage>(this.productsUrl, product);
  }

  delete(id: number): Observable<DataPackage> {
    return this.http.delete<DataPackage>(`${this.productsUrl}/id/${id}`);
  }


}
