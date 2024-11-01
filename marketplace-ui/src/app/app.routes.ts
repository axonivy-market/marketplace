import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { RedirectPageComponent } from './shared/components/redirect-page/redirect-page.component';
import { ERROR_PAGE } from './shared/constants/common.constant';

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
    path: ':id/:version/lib/:artifact',
    component: RedirectPageComponent
  },
  {
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  }
];