import { Routes } from '@angular/router';
import { GithubCallbackComponent } from './auth/github-callback/github-callback.component';
import { ErrorPageComponent } from './shared/components/error-page/error-page.component';
import { ProductArtifactDownloadComponent } from './modules/product/product-artifact-download/product-artifact-download.component';
import { ExternalDocumentComponent } from './shared/components/external-document/external-document.component';

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
    component: ExternalDocumentComponent
  },
  {
    path: 'auth/github/callback',
    component: GithubCallbackComponent
  },
  {
    path: ':id/:version/lib/:artifact',
    component: ProductArtifactDownloadComponent
  }
];