import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { handleHttpError } from './error.interceptor';
import { AdminAuthService } from '../../modules/admin-dashboard/admin-auth.service';

describe('handleHttpError', () => {
  const toastService = {
    publishError: vi.fn(),
    getErrorMessageKey: vi.fn().mockReturnValue('common.error.description.default')
  };
  const router = {
    navigate: vi.fn()
  };
  const adminAuthService = {
    clearToken: vi.fn()
  } as unknown as AdminAuthService;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('redirects unauthenticated write requests to admin login', () => {
    const error = new HttpErrorResponse({ status: 401 });

    handleHttpError(toastService as any, error, '/api/release-letters', 'POST', router as any, adminAuthService, true)
      .subscribe({ error: () => undefined });

    expect(adminAuthService.clearToken).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/admin-login-v2']);
  });

  it('does not redirect GET auth errors', () => {
    const error = new HttpErrorResponse({ status: 401 });

    handleHttpError(toastService as any, error, '/api/release-letters', 'GET', router as any, adminAuthService, true)
      .subscribe({ error: () => undefined });

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('publishes non-auth browser errors', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: { messageDetails: 'server.error' }
    });

    handleHttpError(toastService as any, error, '/api/failure', 'GET', router as any, adminAuthService, true)
      .subscribe({ error: () => undefined });

    expect(toastService.publishError).toHaveBeenCalled();
  });
});
