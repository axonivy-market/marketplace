import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  loadingStates = signal<{ [key: string]: boolean }>({});
  activeCallCount = signal<number>(0);

  private setLoading(componentId: string, isLoading: boolean): void {
    this.loadingStates.update(states => {
      const updatedStates = { ...states };
      updatedStates[componentId] = isLoading;
      return updatedStates;
    });
  }

  showLoading(componentId: string): void {
    this.activeCallCount.set(this.activeCallCount() + 1);
    this.setLoading(componentId, true);
  }

  hideLoading(componentId: string) {
    if (this.activeCallCount() > 0) {
      this.activeCallCount.set(this.activeCallCount() - 1);
    }
    this.setLoading(componentId, false);
  }
}
