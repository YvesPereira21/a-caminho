import { User } from './user';

export interface MunicipalityRequest {
  municipalityName: string;
  user: User;
  cityId: string;
}

export interface MunicipalityResponse {
  municipalityId: string;
  municipalityName: string;
}
