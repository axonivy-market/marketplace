import { CommonModule } from '@angular/common';
import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-admin-token',
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './admin-token.component.html',
  styleUrls: ['./admin-token.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminTokenComponent {
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  isGitHubProcessing = false;

  onSubmit(): void {
    this.isGitHubProcessing = true;
    this.authService.redirectToGitHub('/internal-dashboard', { useAdminState: true });
  }
}
