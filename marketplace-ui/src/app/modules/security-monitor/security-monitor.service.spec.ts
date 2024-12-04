import { TestBed } from '@angular/core/testing';

import { SecurityMonitorService } from './security-monitor.service';

describe('SecurityMonitorService', () => {
  let service: SecurityMonitorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SecurityMonitorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
