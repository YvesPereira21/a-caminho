import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MunicipalityRequest, MunicipalityResponse } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class MunicipalityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/municipalities';

  createMunicipality(municipalityRequestDTO: MunicipalityRequest): Observable<MunicipalityResponse> {
    return this.http.post<MunicipalityResponse>(this.apiUrl, municipalityRequestDTO);
  }

  getMunicipalityByName(municipalityName: string): Observable<MunicipalityResponse> {
    return this.http.get<MunicipalityResponse>(`${this.apiUrl}/${encodeURIComponent(municipalityName)}`);
  }

  getAllMunicipalityFromState(stateName: string): Observable<MunicipalityResponse[]> {
    return this.http.get<MunicipalityResponse[]>(`${this.apiUrl}/state/${encodeURIComponent(stateName)}`);
  }

  deleteMunicipality(municipalityName: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${encodeURIComponent(municipalityName)}`);
  }
}
