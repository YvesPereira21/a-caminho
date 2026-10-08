import { TestBed } from '@angular/core/testing';

import { PollTemplateService } from './poll-template.service';

describe('PollTemplateService', () => {
  let service: PollTemplateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PollTemplateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
