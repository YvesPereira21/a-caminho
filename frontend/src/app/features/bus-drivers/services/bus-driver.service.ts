import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BusDriverRequest, BusDriverResponse } from '../../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BusDriverService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/bus-drivers`;

  createBusDriver(requestDTO: BusDriverRequest): Observable<BusDriverResponse> {
    return this.http.post<BusDriverResponse>(this.apiUrl, requestDTO);
  }

  getBusDriverById(busDriverId: string): Observable<BusDriverResponse> {
    return this.http.get<BusDriverResponse>(`${this.apiUrl}/${busDriverId}`);
  }

  getAllBusDriversFromMunicipality(): Observable<BusDriverResponse[]> {
    return this.http.get<BusDriverResponse[]>(this.apiUrl);
  }

  deleteBusDriverAccount(busDriverId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${busDriverId}`);
  }
}
