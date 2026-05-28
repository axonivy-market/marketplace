import { Component, inject, Input, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { CommonModule } from '@angular/common';
import { NewsManagementService } from '../news-management.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-delete-release-letter-confirm-modal',
  imports: [CommonModule, TranslateModule],
  templateUrl: './delete-release-letter-confirm-modal.component.html',
  styleUrl: './delete-release-letter-confirm-modal.component.scss'
})
export class DeleteReleaseLetterConfirmModalComponent {
  @Input()
  id!: string;

  @Input()
  sprint!: string;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  newsManagementService = inject(NewsManagementService);
  isHandlingApiCall = signal<boolean>(false);

  constructor(public activeModal: NgbActiveModal) {}

  deleteReleaseLetterById() {
    this.isHandlingApiCall.set(true);
    this.newsManagementService
      .deleteReleaseLetterById(this.id)
      .pipe(finalize(() => this.isHandlingApiCall.set(false)))
      .subscribe({
        next: () => {
          this.activeModal.close();
        }
      });
  }
}
