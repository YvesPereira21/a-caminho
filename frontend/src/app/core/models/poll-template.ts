import { Shift } from './enums';
import { PollOptionResponse } from './poll-option';
import { UniversityResponse } from './university';

export interface PollTemplateCreate {
  routeName: string;
  shift: Shift;
  defaultStartTime: string;
  defaultEndTime: string;
  targetUniversityIds: string[];
  defaultOptionIds: string[];
}

export interface PollTemplate {
  templateId: string;
  routeName: string;
  shift: Shift;
  defaultStartTime: string;
  defaultEndTime: string;
  active: boolean;
  targetUniversities: UniversityResponse[];
  defaultOptions: PollOptionResponse[];
}
