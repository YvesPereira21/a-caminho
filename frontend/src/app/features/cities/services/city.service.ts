import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CityRequest, CityResponse } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/cities`;

  createCity(cityRequestDTO: CityRequest): Observable<CityResponse> {
    return this.http.post<CityResponse>(this.apiUrl, cityRequestDTO);
  }

  getCityById(cityId: string): Observable<CityResponse> {
    return this.http.get<CityResponse>(`${this.apiUrl}/${cityId}`);
  }

  getAllCityFromStateByStateName(stateName: string): Observable<CityResponse[]> {
    return this.http.get<CityResponse[]>(`${this.apiUrl}/state/${encodeURIComponent(stateName)}`);
  }

  deleteCity(cityId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${cityId}`);
  }
}
