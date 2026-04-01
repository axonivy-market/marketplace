import type { MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { signal, WritableSignal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { GitHubUser, UserInfo } from '../../../auth/auth.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { GithubUserBadgeComponent } from './github-user-badge.component';

describe('GithubUserBadgeComponent', () => {
  let component: GithubUserBadgeComponent;
  let fixture: ComponentFixture<GithubUserBadgeComponent>;

  let mockAdminAuthService: MockedObject<AdminAuthService> & {
    userInfo: WritableSignal<UserInfo | null>;
  };

  let mockRouter: MockedObject<Router>;

  const mockUser: UserInfo = {
    login: 'mockuser',
    name: 'mockuser',
    avatarUrl: 'https://avatar.url',
    url: 'https://github.com/mockuser',
    token: 'test-token'
  };

  beforeEach(async () => {
    mockAdminAuthService = {
      logout: vi.fn().mockName('AdminAuthService.logout'),
      userInfo: signal<UserInfo | null>(mockUser)
    } as any;

    mockRouter = {
      navigate: vi.fn().mockName('Router.navigate')
    };

    await TestBed.configureTestingModule({
      imports: [GithubUserBadgeComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AdminAuthService, useValue: mockAdminAuthService },
        { provide: Router, useValue: mockRouter },
        { provide: ThemeService, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(GithubUserBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose userInfo signal value', () => {
    expect(component.userInfo()).toEqual(mockUser);
  });

  it('should call logout and navigate to root', () => {
    component.logout();

    expect(mockAdminAuthService.logout).toHaveBeenCalledTimes(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
