import { inject, Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PageTitleService {

  private langSub?: Subscription;
  private readonly translateService = inject(TranslateService);
  private readonly titleService = inject(Title);

  setTitleOnLangChange(titleLabel: string) {
    this.setTitle(titleLabel);

    // Update the title whenever the language changes
    this.langSub = this.translateService.onLangChange.subscribe(() => {
      this.setTitle(titleLabel);
    });
  }

  setTitle(titleLabel: string) {
    this.translateService
      .get(titleLabel)
      .subscribe((translatedTitle: string) => {
        this.titleService.setTitle(translatedTitle);
      });
  }

  ngOnDestroy(): void {
    this.langSub?.unsubscribe();
  }
}
