import { Component, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';

import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { CustomSortCardComponent } from '../custom-sort/custom-sort-card/custom-sort-card.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule
  ],
  templateUrl: './deprecated-management.component.html',
  styleUrl: './deprecated-management.component.scss'
})
export class DeprecatedManagementComponent {
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  extensions = [
    'ai-assistant',
    'approval-decision-utils',
    'html-dialog-demo',
    'asana-connector', "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"
  ];
}
