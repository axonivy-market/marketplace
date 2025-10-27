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

  it('should return false for component that was never set', () => {
    const result = service.isLoading('non-existent-component');
    expect(result).toBeFalse();
  });

  it('should return true when component is loading', () => {
    service.showLoading(LoadingComponentId.DETAIL_PAGE);
    const result = service.isLoading(LoadingComponentId.DETAIL_PAGE);
    expect(result).toBeTrue();
  });

  it('should return false when component is not loading', () => {
    service.hideLoading(LoadingComponentId.DETAIL_PAGE);
    const result = service.isLoading(LoadingComponentId.DETAIL_PAGE);
    expect(result).toBeFalse();
  });

  it('should return correct state after multiple state changes', () => {
    const componentId = LoadingComponentId.DETAIL_PAGE;
    expect(service.isLoading(componentId)).toBeFalse();
    service.showLoading(componentId);
    expect(service.isLoading(componentId)).toBeTrue();
    service.hideLoading(componentId);
    expect(service.isLoading(componentId)).toBeFalse();
    service.showLoading(componentId);
    expect(service.isLoading(componentId)).toBeTrue();
  });
});
