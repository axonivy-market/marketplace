import { CommonModule } from '@angular/common';
import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-admin-token',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './admin-token.component.html',
  styleUrls: ['./admin-token.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminTokenComponent {
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  isProcessing = false;

  onSubmit(): void {
    this.isProcessing = true;
    this.authService.redirectToGitHub('/internal-dashboard');
  }
}
