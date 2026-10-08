import { User } from './user';

export interface BusDriverRequest {
  busDriverName: string;
  user: User;
}

export interface BusDriverResponse {
  busDriverId: string;
  busDriverName: string;
  email: string;
}
