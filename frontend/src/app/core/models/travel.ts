import { PollDirection, TravelStatus } from './enums';
import { BusDriverResponse } from './bus-driver';
import { UniversitySimple } from './university';

export interface TravelCreate {
  pollId: string;
  busId: string;
  status: TravelStatus;
  returnTime: string;
}

export interface Travel {
  travelId: string;
  direction: PollDirection;
  busDriver: BusDriverResponse;
  confirmedVotesCount: number;
  targetUniversities: UniversitySimple[];
}

export interface TravelCancel {
  cancellationReason: string;
}
