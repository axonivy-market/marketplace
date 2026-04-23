import { type MockedObject, beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { errorInterceptor, handleHttpError } from './error.interceptor';
import { HttpToastService } from '../services/browser/http-toast.service';
import { FORBIDDEN, UNAUTHORIZED } from '../../shared/constants/common.constant';

describe('errorInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req, next) =>
    TestBed.runInInjectionContext(() => errorInterceptor(req, next));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  describe('handleHttpError', () => {
    let errorBusService: MockedObject<HttpToastService>;

    beforeEach(() => {
      errorBusService = {
        publishError: vi.fn().mockName('HttpToastService.publishError'),
        getErrorMessageKey: vi.fn().mockName('HttpToastService.getErrorMessageKey')
      } as unknown as MockedObject<HttpToastService>;
    });

    it('should rethrow error without publishing if status is UNAUTHORIZED', () => {
      const error = new HttpErrorResponse({ status: UNAUTHORIZED });

      handleHttpError(errorBusService, error, '/test-url').subscribe({
        error: (err: HttpErrorResponse) => {
          expect(err).toBe(error);
          expect(errorBusService.publishError).not.toHaveBeenCalled();
        }
      });
    });

    it('should rethrow error without publishing if status is FORBIDDEN', () => {
      const error = new HttpErrorResponse({ status: FORBIDDEN });

      handleHttpError(errorBusService, error, '/test-url').subscribe({
        error: (err: HttpErrorResponse) => {
          expect(err).toBe(error);
          expect(errorBusService.publishError).not.toHaveBeenCalled();
        }
      });
    });

    it('should publish error to error bus and rethrow for non-auth errors', () => {
      const error = new HttpErrorResponse({ status: 500 });
      const messageKey = 'common.error.description.500';
      (errorBusService.getErrorMessageKey as any).mockReturnValue(messageKey);

      handleHttpError(errorBusService, error, '/test-url', true).subscribe({
        error: (err: HttpErrorResponse) => {
          expect(err).toBe(error);
        }
      });

      expect(errorBusService.getErrorMessageKey).toHaveBeenCalledWith(500);
      expect(errorBusService.publishError).toHaveBeenCalledWith({
        status: 500,
        messageKey,
        url: '/test-url',
        timestamp: expect.any(Number)
      });
    });

    it('should publish error for 404 status and rethrow', () => {
      const error = new HttpErrorResponse({ status: 404 });
      const messageKey = 'common.error.description.404';
      (errorBusService.getErrorMessageKey as any).mockReturnValue(messageKey);

      handleHttpError(errorBusService, error, '/product/not-found', true).subscribe({
        error: () => {}
      });

      expect(errorBusService.publishError).toHaveBeenCalledWith(
        expect.objectContaining({ status: 404, url: '/product/not-found' })
      );
    });

    it('should NOT publish to error bus on SSR (isBrowser=false)', () => {
      const error = new HttpErrorResponse({ status: 500 });

      handleHttpError(errorBusService, error, '/test-url', false).subscribe({
        error: () => {}
      });

      expect(errorBusService.publishError).not.toHaveBeenCalled();
    });
  });
});
