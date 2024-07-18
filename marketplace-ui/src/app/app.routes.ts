import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';

export const routes: Routes = [
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
