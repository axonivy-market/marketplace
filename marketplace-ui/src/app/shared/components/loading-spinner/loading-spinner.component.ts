import { Component, computed, inject, Input } from '@angular/core';
import { LoadingService } from '../../../core/services/loading/loading.service';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.scss'
})
export class LoadingSpinnerComponent {
  @Input() key: string = '';
  @Input() containerClasses: string = '';
  loadingService = inject(LoadingService);
  isLoading = computed(() => this.loadingService.loadingSubject.value[this.key]);
}
