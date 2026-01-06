import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';

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

  filledToken = '';
  tokenControl = new FormControl('');
  errorMessage = '';
  isProcessing = false;
  isButtonDisabled = true;

  ngOnInit(): void {
    this.tokenControl.setValue('', { emitEvent: false });

    this.tokenControl.valueChanges.subscribe(newValue => {
      this.isButtonDisabled = this.isProcessing || !newValue || newValue === this.filledToken;
    });
  }

  onSubmit(): void {
    this.filledToken = this.tokenControl.value ?? '';
    if (this.isButtonDisabled || !this.filledToken) {
      this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
      return;
    }

    this.isProcessing = true;
    this.tokenControl.disable();
    
    this.authService.requestAccessToken(this.filledToken).subscribe({
      next: (jwtObject) => {
        this.errorMessage = '';
        this.authService.setToken(jwtObject.token);
        this.isProcessing = false;
        this.tokenControl.enable();
        this.router.navigate(['/internal-dashboard']);
      },
      error: (e) => {
        this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
        this.isProcessing = false;
        this.tokenControl.enable();
        this.tokenControl.markAsPristine();
        this.isButtonDisabled = true;
      }
    });
  }

}
