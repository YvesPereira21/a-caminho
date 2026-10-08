import { Shift } from './enums';
import { PollOptionResponse } from './poll-option';
import { UniversityResponse } from './university';

export interface Poll {
  pollId: string;
  routeName: string;
  shift: Shift;
  pollDate: string;
  startTime: string;
  endTime: string;
  totalCapacity?: number;
  confirmedVotesCount: number;
  targetUniversities: UniversityResponse[];
  options: PollOptionResponse[];
}

export interface PollList {
  pollId: string;
  routeName: string;
  shift: Shift;
  pollDate: string;
  startTime: string;
  endTime: string;
  confirmedVotesCount: number;
}

export interface PollPassenger {
  voteId: string;
  studentId: string;
  studentName: string;
  registrationNumber: string;
  courseName: string;
  optionId: string;
  stopName: string;
  returnConfirmed: boolean;
  voteTime: string;
}
