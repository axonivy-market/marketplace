import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GithubCallbackAdminComponent } from './github-callback-admin.component';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth.service';
import { of } from 'rxjs';

describe('GithubCallbackAdminComponent', () => {
  let component: GithubCallbackAdminComponent;
  let fixture: ComponentFixture<GithubCallbackAdminComponent>;
  let mockAuthService: MockedObject<AuthService>;
  let activatedRouteStub: Partial<ActivatedRoute>;

  beforeEach(async () => {
    mockAuthService = {
      handleGitHubAdminCallback: vi.fn().mockName('AuthService.handleGitHubAdminCallback')
    } as MockedObject<AuthService>;
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
    fixture = TestBed.createComponent(GithubCallbackAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call handleGitHubCallback with correct parameters', () => {
    expect(mockAuthService.handleGitHubAdminCallback).toHaveBeenCalledWith(
      'testCode',
      'testState'
    );
  });

  it('should not call handleGitHubCallback if code or state is missing', () => {
    activatedRouteStub.queryParams = of({ code: 'testCode' }); // Missing state
    fixture = TestBed.createComponent(GithubCallbackAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockAuthService.handleGitHubAdminCallback).not.toHaveBeenCalledWith(
      'testCode',
      undefined!
    );

    activatedRouteStub.queryParams = of({ state: 'testState' }); // Missing code
    fixture = TestBed.createComponent(GithubCallbackAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockAuthService.handleGitHubAdminCallback).not.toHaveBeenCalledWith(
      undefined!,
      'testState'
    );
  });
});
