import { TestBed } from '@angular/core/testing';

import { BusDriverService } from './bus-driver.service';

describe('BusDriverService', () => {
  let service: BusDriverService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BusDriverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
