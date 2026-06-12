import { ComponentFixture, TestBed } from '@angular/core/testing';
import { importProvidersFrom } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AdminTokenComponent } from './admin-token.component';
import { AuthService } from '../../../auth/auth.service';

describe('AdminTokenComponent', () => {
  let fixture: ComponentFixture<AdminTokenComponent>;
  let component: AdminTokenComponent;
  const authService = {
    redirectToGitHub: vi.fn()
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTokenComponent],
      providers: [
        importProvidersFrom(TranslateModule.forRoot()),
        {
          provide: AuthService,
          useValue: authService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminTokenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('starts the GitHub login flow', () => {
    component.onSubmit();

    expect(component.isProcessing).toBe(true);
    expect(authService.redirectToGitHub).toHaveBeenCalledWith('/internal-dashboard');
  });
});
