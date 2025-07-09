import { inject, Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Resolve } from "@angular/router";
import { ProductService } from "../../../modules/product/product.service";
import { Observable, of } from "rxjs";
import { CommonUtils } from "../../../shared/utils/common.utils";
import { Meta, Title } from "@angular/platform-browser";

@Injectable({ providedIn: 'root' })
export class TitleResolver implements Resolve<string> {
  meta = inject(Meta);
  titleService = inject(Title);

//   resolve(route: ActivatedRouteSnapshot): string {
//     return "TITLE FROM RESOLVER";
//   }
 resolve(route: ActivatedRouteSnapshot): Observable<string> {
    const title = 'TITLE FROM RESOLVER';
    this.titleService.setTitle(title);
        this.meta.updateTag({ property: 'og:title', content: title });
    return of(title); // must be Observable or Promise for SSR to wait!
  }
}