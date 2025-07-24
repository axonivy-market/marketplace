import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { RedirectPageComponent } from './shared/components/redirect-page/redirect-page.component';
import { ERROR_PAGE } from './shared/constants/common.constant';
import { SecurityMonitorComponent } from './modules/security-monitor/security-monitor.component';
import { ReleasePreviewComponent } from './modules/release-preview/release-preview.component';
import { FeedbackApprovalComponent } from './modules/feedback-approval/feedback-approval.component';
import { MonitoringDashboardComponent } from './modules/monitor/monitor-dashboard/monitor-dashboard.component';
import { ProductDetailResolver } from './core/resolver/product-detail.resolve';
import { ExternalDocumentComponent } from './shared/components/external-document/external-document.component';

export const routes: Routes = [
  // OAuth callback
  {
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  },

  // Error handling more specific first
  {
    path: 'error-page/:id',
    component: ErrorPageComponent,
    title: ERROR_PAGE
  },
  {
    path: 'error-page',
    component: ErrorPageComponent,
    title: ERROR_PAGE
  },

  // Static pages
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
    component: MonitoringDashboardComponent
  },
  {
    path: 'report/:repo/:workflow',
    loadComponent: () => import('./modules/monitor/repo-report/repo-report.component').then(m => m.RepoReportComponent)
  },

  // Document, lib and redirect pages order matters
  {
    path: 'market-cache/:id/:artifact/:version/doc',
    component: ExternalDocumentComponent
  },
  {
    path: 'market-cache/:id/:artifact/:version/doc/index.html',
    component: ExternalDocumentComponent
  },
  {
    path: ':id/:version/doc/index.html',
    component: ExternalDocumentComponent
  },
  {
    path: ':id/:version/doc',
    component: ExternalDocumentComponent
  },
  {
    path: ':id/:version/lib/:artifact',
    component: RedirectPageComponent
  },

  // Product route (dynamic)
  {
    path: ':id',
    loadChildren: () =>
      import('./modules/product/product.routes').then(m => m.routes),
    resolve: {
      productDetail: ProductDetailResolver
    }
  },

  // Home module (static root)
  {
    path: '',
    loadChildren: () =>
      import('./modules/home/home.routes').then(m => m.routes)
  },

  // Wildcard route for unmatched paths (404)
  {
    path: '**',
    redirectTo: 'error-page/404'
  }
];
