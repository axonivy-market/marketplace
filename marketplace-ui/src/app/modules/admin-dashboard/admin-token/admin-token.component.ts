import { CommonModule } from '@angular/common';
import { Component, inject, Input, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-admin-token',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './admin-token.component.html',
  styleUrls: ['./admin-token.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminTokenComponent {
  themeService = inject(ThemeService);
  @Input() errorMessage = '';

  authService = inject(AdminAuthService);
  router = inject(Router);
  token = '';

  onSubmit(): void {
    if (!this.token) {
      this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
      return;
    }

    this.authService.setToken(this.token);
    this.router.navigate(['/internal-dashboard']);
  }
}
