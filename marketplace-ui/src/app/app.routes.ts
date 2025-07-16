import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { RedirectPageComponent } from './shared/components/redirect-page/redirect-page.component';
import { ERROR_PAGE } from './shared/constants/common.constant';
import { SecurityMonitorComponent } from './modules/security-monitor/security-monitor.component';
import { ReleasePreviewComponent } from './modules/release-preview/release-preview.component';
import { FeedbackApprovalComponent } from './modules/feedback-approval/feedback-approval.component';

export const routes: Routes = [
  {
    path: 'error-page',
    component: ErrorPageComponent,
    title: ERROR_PAGE
  },
  {
    path: 'error-page/:id',
    component: ErrorPageComponent,
    title: ERROR_PAGE
  },
  {
    path: 'security-monitor',
    component: SecurityMonitorComponent
  },
  {
    path: 'release-preview',
    component: ReleasePreviewComponent
  },
  {
    path: 'feedback-approval',
    component: FeedbackApprovalComponent
  },
  {
    path: 'monitoring',
    component: DashboardComponent
  },
  {
    path: 'report/:repo/:workflow',
    loadComponent: () => import('./modules/monitor/repo-report/repo-report.component').then(m => m.RepoReportComponent)
  },
  {
    path: '',
    loadChildren: () => import('./modules/home/home.routes').then(m => m.routes)
  },
  {
    path: ':id',
    loadChildren: () =>
      import('./modules/product/product.routes').then(m => m.routes)
  },
  {
    path: ':id/:version/doc',
    component: RedirectPageComponent
  },
  {
    path: ':id/:version/doc/index.html',
    component: RedirectPageComponent
  },
  {
    path: ':id/:version/lib/:artifact',
    component: RedirectPageComponent
  },
  {
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  }
];
