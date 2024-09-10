import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponentComponent } from './shared/components/error-page-component/error-page-component.component';

export const routes: Routes = [
  {
    path: 'error-page',
    component: ErrorPageComponentComponent
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
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  }
];
