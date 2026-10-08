import { TestBed } from '@angular/core/testing';

import { UniversityStudentService } from './university-student.service';

describe('UniversityStudentService', () => {
  let service: UniversityStudentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UniversityStudentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
