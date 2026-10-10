import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PollOption, PollOptionResponse } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PollOptionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/poll-options`;

  createPollOption(pollOptionDTO: PollOption): Observable<PollOptionResponse> {
    return this.http.post<PollOptionResponse>(this.apiUrl, pollOptionDTO);
  }

  getAllPollOptions(): Observable<PollOptionResponse[]> {
    return this.http.get<PollOptionResponse[]>(this.apiUrl);
  }

  getPollOption(optionId: string): Observable<PollOptionResponse> {
    return this.http.get<PollOptionResponse>(`${this.apiUrl}/${optionId}`);
  }

  updatePollOption(optionId: string, pollOptionDTO: PollOption): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${optionId}`, pollOptionDTO);
  }

  deletePollOption(optionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${optionId}`);
  }
}
