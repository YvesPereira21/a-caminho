import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PollPassenger, Travel, TravelCancel, TravelCreate } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class TravelService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/travels';

  createTravel(travelDTO: TravelCreate): Observable<Travel> {
    return this.http.post<Travel>(this.apiUrl, travelDTO);
  }

  getTravelById(travelId: string): Observable<Travel> {
    return this.http.get<Travel>(`${this.apiUrl}/${travelId}`);
  }

  getActiveTravelByDriver(): Observable<Travel> {
    return this.http.get<Travel>(`${this.apiUrl}/active`);
  }

  getAllTravelsByDriver(): Observable<Travel[]> {
    return this.http.get<Travel[]>(`${this.apiUrl}/driver`);
  }

  getAllTravelsByMunicipality(): Observable<Travel[]> {
    return this.http.get<Travel[]>(`${this.apiUrl}/municipality`);
  }

  getTravelPassengers(travelId: string): Observable<PollPassenger[]> {
    return this.http.get<PollPassenger[]>(`${this.apiUrl}/${travelId}/passengers`);
  }

  startReturn(travelId: string): Observable<Travel> {
    return this.http.patch<Travel>(`${this.apiUrl}/${travelId}/start-return`, {});
  }

  completeTravel(travelId: string): Observable<Travel> {
    return this.http.patch<Travel>(`${this.apiUrl}/${travelId}/complete`, {});
  }

  cancelTravel(travelId: string, cancelDTO: TravelCancel): Observable<Travel> {
    return this.http.patch<Travel>(`${this.apiUrl}/${travelId}/cancel`, cancelDTO);
  }
}
