import { ComponentFixture, TestBed } from '@angular/core/testing';

import { signal, WritableSignal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { GitHubUser } from '../../../auth/auth.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { GithubUserBadgeComponent } from './github-user-badge.component';

describe('GithubUserBadgeComponent', () => {
  let component: GithubUserBadgeComponent;
  let fixture: ComponentFixture<GithubUserBadgeComponent>;

  let mockAdminAuthService: jasmine.SpyObj<AdminAuthService> & {
    adminInfo: WritableSignal<GitHubUser | null>;
  };

  let mockRouter: jasmine.SpyObj<Router>;

  const mockUser: GitHubUser = {
    login: 'mockuser',
    name: 'mockuser',
    avatarUrl: 'https://avatar.url',
    url: 'https://github.com/mockuser'
  };

  beforeEach(async () => {
    mockAdminAuthService = jasmine.createSpyObj(
      'AdminAuthService',
      ['logout'],
      {
        adminInfo: signal<GitHubUser | null>(mockUser)
      }
    ) as any;

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

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

  it('should expose adminInfo signal value', () => {
    expect(component.adminInfo()).toEqual(mockUser);
  });

  it('should call logout and navigate to root', () => {
    component.logout();

    expect(mockAdminAuthService.logout).toHaveBeenCalledTimes(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
