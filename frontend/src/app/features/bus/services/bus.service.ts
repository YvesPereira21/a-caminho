import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bus, BusCreate, BusLocation, BusUpdate } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BusService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/buses`;

  createBus(busCreateDTO: BusCreate): Observable<Bus> {
    return this.http.post<Bus>(this.apiUrl, busCreateDTO);
  }

  getBusById(busId: string): Observable<Bus> {
    return this.http.get<Bus>(`${this.apiUrl}/${busId}`);
  }

  getLatestLocation(busId: string): Observable<BusLocation> {
    return this.http.get<BusLocation>(`${this.apiUrl}/${busId}/location`);
  }

  getAllBusesByMunicipality(): Observable<Bus[]> {
    return this.http.get<Bus[]>(this.apiUrl);
  }

  getAllBusesForDriver(): Observable<Bus[]> {
    return this.http.get<Bus[]>(`${this.apiUrl}/driver`);
  }

  updateBus(busId: string, busUpdateDTO: BusUpdate): Observable<Bus> {
    return this.http.put<Bus>(`${this.apiUrl}/${busId}`, busUpdateDTO);
  }

  deleteBus(busId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${busId}`);
  }
}
