import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Poll, PollList } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class PollService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/polls';

  getPoll(pollId: string): Observable<Poll> {
    return this.http.get<Poll>(`${this.apiUrl}/${pollId}`);
  }

  getAllPollsFromMunicipality(): Observable<PollList[]> {
    return this.http.get<PollList[]>(this.apiUrl);
  }

  getOpenPollsToStudent(): Observable<PollList[]> {
    return this.http.get<PollList[]>(`${this.apiUrl}/open`);
  }
}
