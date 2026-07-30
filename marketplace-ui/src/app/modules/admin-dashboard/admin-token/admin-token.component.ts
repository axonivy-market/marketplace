import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewEncapsulation, ChangeDetectorRef, NgZone } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Router } from '@angular/router';
import { AdminAuthService } from '../admin-auth.service';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';
import { UserInfo } from '../../../auth/auth.service';

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
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly ngZone = inject(NgZone);

  filledToken = '';
  tokenControl = new FormControl('');
  errorMessage = '';
  isProcessing = false;
  isButtonDisabled = true;

  ngOnInit(): void {
    this.tokenControl.valueChanges.subscribe(newValue => {
      this.ngZone.run(() => {
        this.isButtonDisabled = this.isProcessing || !newValue || newValue === this.filledToken;
        if (!this.isButtonDisabled) {
          this.errorMessage = '';
        }
        this.cdr.markForCheck();
      });
    });
  }

  onSubmit(): void {
    this.filledToken = this.tokenControl.value ?? '';
    this.isProcessing = true;
    this.tokenControl.disable();
    
    this.authService.requestAccessToken(this.filledToken).subscribe({
      next: (userInfo: UserInfo) => {
        this.ngZone.run(() => {
          this.errorMessage = '';
          this.authService.setUserInfo(userInfo);
          this.isProcessing = false;
          this.tokenControl.enable();
          this.router.navigate(['/internal-dashboard']);
          this.cdr.markForCheck();
        });
      },
      error: () => {
        this.ngZone.run(() => {
          this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
          this.isProcessing = false;
          this.tokenControl.enable();
          this.tokenControl.markAsPristine();
          this.isButtonDisabled = true;
          this.cdr.markForCheck();
        });
      }
    });
  }
}
