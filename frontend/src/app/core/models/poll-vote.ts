export interface VoteRequest {
  optionId: string;
  returnConfirmed?: boolean;
}

export interface VoteResponse {
  voteId: string;
  pollId: string;
  optionId: string;
  stopName: string;
  returnConfirmed: boolean;
  voteTime: string;
}
