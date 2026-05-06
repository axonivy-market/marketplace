import { afterEach, describe, expect, it, vi } from 'vitest';
import { HttpToastService, type HttpErrorEvent } from './http-toast.service';
import {
  BAD_GATEWAY,
  BAD_REQUEST_ERROR_CODE,
  FORBIDDEN,
  GATEWAY_TIMEOUT,
  INTERNAL_SERVER_ERROR_CODE,
  NOT_FOUND_ERROR_CODE,
  REQUEST_TIMEOUT,
  SERVICE_UNAVAILABLE,
  UNAUTHORIZED
} from '../../../shared/constants/common.constant';

describe('HttpToastService', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });
  let mockError: HttpErrorEvent = {
    status: 500,
    messageKey: 'common.error.description.500',
    url: '/api/products',
    timestamp: 1000
  };

  it('should publish an error event', () => {
    const service = new HttpToastService();
    const events: HttpErrorEvent[] = [];

    service.getError().subscribe(event => events.push(event));

    service.publishError(mockError);

    expect(events).toEqual([mockError]);
  });

  it('should dedupe repeated message keys inside dedupe window', () => {
    const service = new HttpToastService();
    const events: HttpErrorEvent[] = [];

    service.getError().subscribe(event => events.push(event));
    mockError.timestamp = 1000;
    const nowSpy = vi.spyOn(Date, 'now');
    nowSpy.mockReturnValue(1000);
    service.publishError(mockError);

    nowSpy.mockReturnValue(2000);
    mockError.timestamp = 2000;
    service.publishError(mockError);

    expect(events).toHaveLength(1);
  });

  it('should allow same message key after dedupe window', () => {
    const service = new HttpToastService();
    const events: HttpErrorEvent[] = [];

    service.getError().subscribe(event => events.push(event));

    const nowSpy = vi.spyOn(Date, 'now');
    nowSpy.mockReturnValue(1000);
    service.publishError(mockError);

    nowSpy.mockReturnValue(2600);
    mockError.timestamp = 2600;
    service.publishError(mockError);

    expect(events).toHaveLength(2);
  });

  it('should emit clear signal', () => {
    const service = new HttpToastService();
    const clearSpy = vi.fn();

    service.getClear().subscribe(clearSpy);
    service.clearError();

    expect(clearSpy).toHaveBeenCalledTimes(1);
  });

  it('should map status codes to message keys', () => {
    const service = new HttpToastService();

    expect(service.getErrorMessageKey(BAD_REQUEST_ERROR_CODE)).toBe('common.error.description.badRequest');
    expect(service.getErrorMessageKey(UNAUTHORIZED)).toBe('common.error.description.unauthorized');
    expect(service.getErrorMessageKey(FORBIDDEN)).toBe('common.error.description.forbidden');
    expect(service.getErrorMessageKey(NOT_FOUND_ERROR_CODE)).toBe('common.error.description.404');
    expect(service.getErrorMessageKey(REQUEST_TIMEOUT)).toBe('common.error.description.timeout');
    expect(service.getErrorMessageKey(INTERNAL_SERVER_ERROR_CODE)).toBe('common.error.description.500');
    expect(service.getErrorMessageKey(BAD_GATEWAY)).toBe('common.error.description.badGateway');
    expect(service.getErrorMessageKey(SERVICE_UNAVAILABLE)).toBe('common.error.description.serviceUnavailable');
    expect(service.getErrorMessageKey(GATEWAY_TIMEOUT)).toBe('common.error.description.gatewayTimeout');
    expect(service.getErrorMessageKey(999)).toBe('common.error.description.default');
  });
});
