import { Component, inject, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { CommonModule } from '@angular/common';
import { AdminDashboardService } from '../../admin-dashboard.service';

@Component({
  selector: 'app-delete-release-letter-confirm-modal',
  imports: [CommonModule, TranslateModule],
  templateUrl: './delete-release-letter-confirm-modal.component.html',
  styleUrl: './delete-release-letter-confirm-modal.component.scss'
})
export class DeleteReleaseLetterConfirmModalComponent {
  @Input()
  releaseLetterSprint!: string;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  adminDashboardService = inject(AdminDashboardService);

  constructor(public activeModal: NgbActiveModal) {}

  deleteReleaseLetterBySprint() {
    this.adminDashboardService
      .deleteReleaseLetterBySprint(this.releaseLetterSprint)
      .subscribe({
        next: () => {
          this.activeModal.close();
        }
      });
  }
}
