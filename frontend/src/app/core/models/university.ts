export interface UniversityRequest {
  name: string;
  campus?: string;
  cityName: string;
  stateName: string;
}

export interface UniversityResponse {
  id: string;
  name: string;
  campus?: string;
  cityName: string;
  stateName: string;
}

export interface UniversitySimple {
  universityId: string;
  campus: string;
}
