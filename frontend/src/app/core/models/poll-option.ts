export interface PollOption {
  stopName: string;
}

export interface PollOptionResponse {
  optionId: string;
  stopName: string;
  voteCount: number;
}
