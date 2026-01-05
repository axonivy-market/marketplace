import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';
import { startWith } from 'rxjs';

@Component({
  selector: 'app-admin-token',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './admin-token.component.html',
  styleUrls: ['./admin-token.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminTokenComponent implements OnInit {
  themeService = inject(ThemeService);
  authService = inject(AdminAuthService);
  router = inject(Router);

  token = '';
  tokenControl = new FormControl('');
  errorMessage = '';
  isProcessing = false;
  isButtonDisabled = true;

  ngOnInit(): void {
    this.token = this.authService.token ?? '';
    this.tokenControl.setValue(this.token, { emitEvent: false });

    this.tokenControl.valueChanges.subscribe(newValue => {
      this.isButtonDisabled = this.isProcessing || !newValue || newValue === this.token;
    });
  }

  onSubmit(): void {
    const token = this.tokenControl.value;
    if (this.isButtonDisabled || !token) {
      this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
      return;
    }

    this.isProcessing = true;
    
    this.authService.validateToken(token).subscribe({
      next: (jwt) => {
        this.errorMessage = '';
        this.authService.setToken(jwt);
        this.isProcessing = false;
        this.router.navigate(['/internal-dashboard']);
      },
      error: (e) => {
        this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
        this.isProcessing = false;
        this.token = this.tokenControl.value ?? '';
        this.tokenControl.markAsPristine();
        this.isButtonDisabled = true;
      }
    });
  }

}
