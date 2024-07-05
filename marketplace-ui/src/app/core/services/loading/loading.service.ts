import { Injectable, computed, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private readonly isShow = signal(false);
  isLoading = computed(() => this.isShow());

  show() {
    this.isShow.set(true);
  }

  hide() {
    this.isShow.set(false);
  }
}
