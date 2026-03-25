import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminTokenComponent } from './admin-token.component';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';
import { of, throwError } from 'rxjs';
import { UserInfo } from '../../../auth/auth.service';

const mockUser: UserInfo = {
  login: 'octocat',
  name: 'The Octocat',
  avatarUrl: 'https://avatar.url',
  url: 'https://github.com/octocat',
  token: 'test-token'
};

describe('AdminTokenComponent', () => {
  let component: AdminTokenComponent;
  let fixture: ComponentFixture<AdminTokenComponent>;
  let authService: jasmine.SpyObj<AdminAuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AdminAuthService', [
      'setUserInfo',
      'logout',
      'loadFromSession',
      'requestAccessToken'
    ]);
    router = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: AdminAuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminTokenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with button disabled', () => {
    expect(component.isButtonDisabled).toBe(true);
    expect(component.tokenControl.value).toBe('');
  });

  it('should enable button when token is entered', () => {
    component.tokenControl.setValue('some-token');

    expect(component.isButtonDisabled).toBe(false);
  });

  it('should disable button when token matches filledToken', () => {
    component.filledToken = 'same-token';
    component.tokenControl.setValue('same-token');

    expect(component.isButtonDisabled).toBe(true);
  });

  describe('onSubmit', () => {
    it('should call requestAccessToken and navigate on success', () => {
      authService.requestAccessToken.and.returnValue(of(mockUser));
      component.tokenControl.setValue('valid-github-token');

      component.onSubmit();

      expect(component.isProcessing).toBe(false);
      expect(authService.requestAccessToken).toHaveBeenCalledWith('valid-github-token');
      expect(authService.setUserInfo).toHaveBeenCalledWith(mockUser);
      expect(router.navigate).toHaveBeenCalledWith(['/internal-dashboard']);
      expect(component.errorMessage).toBe('');
    });

    it('should set error message when requestAccessToken fails', () => {
      authService.requestAccessToken.and.returnValue(
        throwError(() => new Error('Invalid token'))
      );
      component.tokenControl.setValue('invalid-token');

      component.onSubmit();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.INVALID_TOKEN);
      expect(component.isProcessing).toBe(false);
      expect(component.isButtonDisabled).toBe(true);
      expect(authService.setUserInfo).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should disable control during processing', () => {
      authService.requestAccessToken.and.returnValue(of(mockUser));
      component.tokenControl.setValue('valid-token');

      component.onSubmit();

      expect(component.tokenControl.enabled).toBe(true);
    });

    it('should re-enable control after error', () => {
      authService.requestAccessToken.and.returnValue(throwError(() => new Error('Error')));
      component.tokenControl.setValue('invalid-token');

      component.onSubmit();

      expect(component.tokenControl.enabled).toBe(true);
    });
  });
});
