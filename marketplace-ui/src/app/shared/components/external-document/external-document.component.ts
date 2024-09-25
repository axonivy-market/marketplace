import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';

const INDEX_FILE = '/index.html';
const DOC_API = 'api/product-doc';

@Component({
  selector: 'app-external-document',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>"
})
export class ExternalDocumentComponent implements OnInit {
  httpClient = inject(HttpClient);
  constructor(
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    const product = this.route.snapshot.paramMap.get(ROUTER.ID);
    const version = this.route.snapshot.paramMap.get(ROUTER.VERSION);
    const currentUrl = window.location.href;

    this.httpClient.get<string>(`${DOC_API}/${product}/${version}`)
      .subscribe((response: any) => {
        if (response == null || response === ''
          || currentUrl === response || currentUrl + INDEX_FILE === response) {
          console.log('No redirection needed, the URLs are the same.');
        } else {
          window.location.href = response;
        }
      });
  }

}
