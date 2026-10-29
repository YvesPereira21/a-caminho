import { PollDirection, Shift, TravelStatus } from './enums';
import { BusDriverResponse } from './bus-driver';
import { UniversitySimple } from './university';
import { Bus } from './bus';

export interface TravelCreate {
  pollId: string;
  busId: string;
  status: TravelStatus;
  returnTime: string;
  direction?: PollDirection;
}

export interface Travel {
  travelId: string;
  status: TravelStatus;
  direction: PollDirection;
  travelDate?: string;
  departureTime?: string;
  returnTime?: string;
  shift?: Shift;
  bus?: Bus;
  pollId?: string;
  busDriver: BusDriverResponse;
  confirmedVotesCount: number;
  targetUniversities: UniversitySimple[];
}

export interface TravelCancel {
  cancellationReason: string;
}
