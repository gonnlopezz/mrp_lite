import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DataPackage } from '../data-package';
import { TipoEquipo } from './tipo-equipo';

@Injectable({
  providedIn: 'root'
})
export class TipoEquipoService {


  private tipoEquiposUrl = "rest/equipment-types";
  constructor(private http: HttpClient) { }

  all(): Observable<DataPackage> {
    return this.http.get<DataPackage>(this.tipoEquiposUrl);
  }

  byPage(page: number, size: number): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.tipoEquiposUrl}/page?page=${page - 1}&size=${size}`);
  }

  get(id: string): Observable<DataPackage> {
    return this.http.get<DataPackage>(`${this.tipoEquiposUrl}/id/${id}`);
  }

  save(tipoEquipo: TipoEquipo): Observable<DataPackage> {
    return tipoEquipo.id ?
      this.http.put<DataPackage>(this.tipoEquiposUrl, tipoEquipo) :
      this.http.post<DataPackage>(this.tipoEquiposUrl, tipoEquipo);
  }

  delete(id: number): Observable<DataPackage> {
    return this.http.delete<DataPackage>(`${this.tipoEquiposUrl}/id/${id}`);
  }


}
