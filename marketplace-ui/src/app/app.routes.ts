import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { RedirectPageComponent } from './shared/components/redirect-page/redirect-page.component';
import { ERROR_PAGE } from './shared/constants/common.constant';
import { SecurityMonitorComponent } from './modules/admin-dashboard/security-monitor/security-monitor.component';
import { ReleasePreviewComponent } from './modules/release-preview/release-preview.component';
import { MonitoringDashboardComponent } from './modules/monitor/monitor-dashboard/monitor-dashboard.component';
import { ProductDetailResolver } from './core/resolver/product-detail.resolve';
import { AdminDashboardComponent } from './modules/admin-dashboard/admin-dashboard.component';
import { CustomSortComponent } from './modules/admin-dashboard/custom-sort/custom-sort.component';
import { FeedbackApprovalComponent } from './modules/admin-dashboard/feedback-approval/feedback-approval.component';
import { QuickAccessComponent } from './modules/admin-dashboard/quick-access/quick-access.component';
import { AdminAuthGuard } from './modules/admin-dashboard/admin-auth.guard';

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
    path: 'release-preview',
    component: ReleasePreviewComponent
  },
  {
    path: 'monitoring',
    component: MonitoringDashboardComponent
  },
  {
    path: 'internal-dashboard',
    component: AdminDashboardComponent,
    canActivate: [AdminAuthGuard],
    children: [
      {
        path: 'security-monitor',
        component: SecurityMonitorComponent
      },
      {
        path: 'feedback-approval',
        component: FeedbackApprovalComponent
      },
      {
        path: 'sorting',
        component: CustomSortComponent
      },
      {
        path: 'quick-access',
        component: QuickAccessComponent
      }
    ]
  },
  {
    path: 'monitoring/:repo/:workflow',
    loadComponent: () => import('./modules/monitor/repo-report/repo-report.component').then(m => m.RepoReportComponent)
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
