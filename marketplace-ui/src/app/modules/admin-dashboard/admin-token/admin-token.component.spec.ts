import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminTokenComponent } from './admin-token.component';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';

describe('AdminTokenComponent', () => {
  let component: AdminTokenComponent;
  let fixture: ComponentFixture<AdminTokenComponent>;
  let authService: jasmine.SpyObj<AdminAuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AdminAuthService', ['setToken']);
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

  describe('onSubmit', () => {
    it('should set error message when token is empty', () => {
      component.token = '';

      component.onSubmit();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.TOKEN_REQUIRED);
      expect(authService.setToken).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should save token and navigate when token is valid', () => {
      component.token = 'valid-token';

      component.onSubmit();

      expect(authService.setToken).toHaveBeenCalledWith('valid-token');
      expect(router.navigate).toHaveBeenCalledWith(['/internal-dashboard']);
      expect(component.errorMessage).toBe('');
    });

    it('should trim and save token when it has whitespace', () => {
      component.token = '  valid-token  ';

      component.onSubmit();

      expect(authService.setToken).toHaveBeenCalledWith('  valid-token  ');
      expect(router.navigate).toHaveBeenCalledWith(['/internal-dashboard']);
    });
  });
});
