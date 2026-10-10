import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PollTemplate, PollTemplateCreate } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PollTemplateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/poll-templates`;

  createPollTemplate(pollTemplateCreateDTO: PollTemplateCreate): Observable<PollTemplate> {
    return this.http.post<PollTemplate>(this.apiUrl, pollTemplateCreateDTO);
  }

  getAllPollTemplates(): Observable<PollTemplate[]> {
    return this.http.get<PollTemplate[]>(this.apiUrl);
  }

  getPollTemplate(templateId: string): Observable<PollTemplate> {
    return this.http.get<PollTemplate>(`${this.apiUrl}/${templateId}`);
  }

  deactivatePollTemplate(templateId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${templateId}/deactivate`, {});
  }

  deletePollTemplate(templateId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${templateId}`);
  }
}
