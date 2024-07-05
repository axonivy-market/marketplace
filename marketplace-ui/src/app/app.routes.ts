import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./modules/home/home.routes').then((m) => m.routes),
  },
  {
    path: ':id',
    loadChildren: () =>
      import('./modules/product/product.routes').then((m) => m.routes),
  },
];
