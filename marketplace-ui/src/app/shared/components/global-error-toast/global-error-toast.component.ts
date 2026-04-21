import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import {
  HttpErrorBusService,
  HttpErrorEvent
} from '../../../core/services/http-error-bus.service';
import { Subject, takeUntil, timer } from 'rxjs';

@Component({
  selector: 'app-global-error-toast',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './global-error-toast.component.html',
  styleUrl: './global-error-toast.component.scss'
})
export class GlobalErrorToastComponent implements OnInit, OnDestroy {
  errorBus = inject(HttpErrorBusService);
  private destroy$ = new Subject<void>();

  currentError: HttpErrorEvent | null = null;
  isVisible = false;
  autoHideDuration = 8000;

  ngOnInit(): void {
    this.errorBus.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.currentError = error;
        this.isVisible = true;
        this.scheduleAutoHide();
      });

    this.errorBus.clear$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.dismiss();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  dismiss(): void {
    this.isVisible = false;
    this.currentError = null;
  }

  private scheduleAutoHide(): void {
    timer(this.autoHideDuration)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.isVisible) {
          this.dismiss();
        }
      });
  }
}
