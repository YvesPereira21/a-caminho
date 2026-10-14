export interface BusCreate {
  busName: string;
  seatsQuantity: number;
}

export interface Bus {
  busId: string;
  busName: string;
  seatsQuantity: number;
}

export interface BusUpdate {
  busName?: string;
  seatsQuantity?: number;
}

export interface BusLocation {
  busId: string;
  latitude: number;
  longitude: number;
  timestamp?: string;
}

