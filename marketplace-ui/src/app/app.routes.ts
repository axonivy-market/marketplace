import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { RedirectPageComponent } from './shared/components/redirect-page/redirect-page.component';

export const routes: Routes = [
  {
    path: 'error-page',
    component: ErrorPageComponent
  },
  {
    path: 'error-page/:id',
    component: ErrorPageComponent
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
    path: ':id/:version/:artifact/:fileType',
    component: RedirectPageComponent
  },
  {
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  }
];