import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UniversityRequest, UniversityResponse } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class UniversityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/universities';

  createUniversity(requestDTO: UniversityRequest): Observable<UniversityResponse> {
    return this.http.post<UniversityResponse>(this.apiUrl, requestDTO);
  }

  getAllUniversityByNameFromState(universityName: string, stateName: string): Observable<UniversityResponse[]> {
    const params = new HttpParams()
      .set('universityName', universityName)
      .set('stateName', stateName);
    return this.http.get<UniversityResponse[]>(this.apiUrl, { params });
  }

  deleteUniversity(universityId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${universityId}`);
  }
}
