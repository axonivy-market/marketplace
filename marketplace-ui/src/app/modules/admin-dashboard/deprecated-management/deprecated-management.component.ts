import { Component, inject } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';

import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { CustomSortCardComponent } from '../custom-sort/custom-sort-card/custom-sort-card.component';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { SyncTaskRow } from '../../../shared/models/sync-task-execution.model';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule,
    NgClass
  ],
  templateUrl: './deprecated-management.component.html',
  styleUrl: './deprecated-management.component.scss'
})
export class DeprecatedManagementComponent {
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  showDeprecatedProductDialog = false;
  extensions = [
    'ai-assistant',
    'approval-decision-utils',
    'html-dialog-demo',
    'asana-connector'
  ];
  isClosing = false;
  syncData = {
    extensionId: '',
    marketItemPath: '',
    override: false
  };

  trigger() {
    this.showDeprecatedProductDialog = true;
  }

  closeDialog() {
    this.showDeprecatedProductDialog = false;
  }

  synchronize() {
    console.log('Sync data:', this.syncData);

    // TODO: call API here

    this.closeDialog();
  }
}
