import { TestBed } from '@angular/core/testing';

import { LoadingService } from './loading.service';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';

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
    service.showLoading(LoadingComponentId.DETAIL_PAGE);
    expect(service.loadingStates()[LoadingComponentId.DETAIL_PAGE]).toBeTrue();
  })

  it('hide should update isLoading to false', () => {
    service.hideLoading(LoadingComponentId.DETAIL_PAGE);
    expect(service.loadingStates()[LoadingComponentId.DETAIL_PAGE]).toBeFalse();
  })
});
