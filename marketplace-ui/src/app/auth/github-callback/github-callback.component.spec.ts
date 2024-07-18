import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GithubCallbackComponent } from './github-callback.component';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth.service';
import { of } from 'rxjs';

describe('GithubCallbackComponent', () => {
  let component: GithubCallbackComponent;
  let fixture: ComponentFixture<GithubCallbackComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let activatedRouteStub: Partial<ActivatedRoute>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['handleGitHubCallback']);
    activatedRouteStub = {
      queryParams: of({ code: 'testCode', state: 'testState' })
    };

    await TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ActivatedRoute, useValue: activatedRouteStub }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GithubCallbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call handleGitHubCallback with correct parameters', () => {
    expect(mockAuthService.handleGitHubCallback).toHaveBeenCalledWith('testCode', 'testState');
  });

  it('should not call handleGitHubCallback if code or state is missing', () => {
    activatedRouteStub.queryParams = of({ code: 'testCode' }); // Missing state
    fixture = TestBed.createComponent(GithubCallbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockAuthService.handleGitHubCallback).not.toHaveBeenCalledWith('testCode', undefined!);

    activatedRouteStub.queryParams = of({ state: 'testState' }); // Missing code
    fixture = TestBed.createComponent(GithubCallbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockAuthService.handleGitHubCallback).not.toHaveBeenCalledWith(undefined!, 'testState');
  });
});
