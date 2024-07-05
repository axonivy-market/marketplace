import { TestBed } from '@angular/core/testing';

import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('show should update isLoading to true', () => {
    service.show();
    expect(service.isLoading()).toBeTrue();
  })

  it('hide should update isLoading to false', () => {
    service.hide();
    expect(service.isLoading()).toBeFalse();
  })
});
