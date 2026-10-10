import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { State } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/states`;

  getAllState(): Observable<State[]> {
    return this.http.get<State[]>(this.apiUrl);
  }

  createState(stateDTO: State): Observable<State> {
    return this.http.post<State>(this.apiUrl, stateDTO);
  }

  deleteState(stateName: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${encodeURIComponent(stateName)}`);
  }
}
