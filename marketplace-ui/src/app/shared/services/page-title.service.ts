import { inject, Injectable } from '@angular/core';
import { TranslateService, LangChangeEvent } from '@ngx-translate/core';
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
    // Initial title set
    this.translateService
      .get(titleLabel)
      .subscribe((translated: string) => {
        this.titleService.setTitle(translated);
      });

    // Update the title whenever the language changes
    this.langSub = this.translateService.onLangChange.subscribe(
      (event: LangChangeEvent) => {
        this.translateService
          .get(titleLabel)
          .subscribe((translated: string) => {
            this.titleService.setTitle(translated);
          });
      }
    );
  }

  ngOnDestroy(): void {
    this.langSub?.unsubscribe();
  }
}
