import { Component, inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { NgbToast } from '@ng-bootstrap/ng-bootstrap';
import { Subject, takeUntil } from 'rxjs';
import { HttpToastService, HttpErrorEvent } from '../../../core/services/browser/http-toast.service';

@Component({
  selector: 'app-global-toast',
  standalone: true,
  imports: [CommonModule, TranslateModule, NgbToast],
  templateUrl: './global-toast.component.html',
  styleUrl: './global-toast.component.scss'
})
export class GlobalToastComponent implements OnInit, OnDestroy {
  toastService = inject(HttpToastService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly destroy$ = new Subject<void>();

  currentError: HttpErrorEvent | null = null;
  isVisible = false;
  autoHideDuration = 8000;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.toastService.getError()
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.currentError = error;
        this.isVisible = true;
      });

    this.toastService.getClear()
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
}
