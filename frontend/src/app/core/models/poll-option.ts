export interface PollOption {
  stopName: string;
  latitude?: number;
  longitude?: number;
}

export interface PollOptionResponse {
  optionId: string;
  stopName: string;
  voteCount: number;
  latitude?: number;
  longitude?: number;
}
