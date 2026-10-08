import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PollPassenger, VoteRequest, VoteResponse } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class PollVoteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/polls';

  vote(pollId: string, voteRequestDTO: VoteRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${pollId}/votes`, voteRequestDTO);
  }

  getMyVote(pollId: string): Observable<VoteResponse> {
    return this.http.get<VoteResponse>(`${this.baseUrl}/${pollId}/my-vote`);
  }

  getPollPassengers(pollId: string): Observable<PollPassenger[]> {
    return this.http.get<PollPassenger[]>(`${this.baseUrl}/${pollId}/passengers`);
  }

  cancelVote(pollId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${pollId}/votes`);
  }
}
