import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  loadingStates = signal<{ [key: string]: boolean }>({});

  private setLoading(componentId: string, isLoading: boolean): void {
    this.loadingStates.update(states => {
      const updatedStates = { ...states };
      updatedStates[componentId] = isLoading;
      return updatedStates;
    });
  }

  showLoading(componentId: string): void {
    this.setLoading(componentId, true);
  }

  hideLoading(componentId: string) {
    this.setLoading(componentId, false);
  }

  isLoading(componentId: string): boolean {
    return this.loadingStates()[componentId] || false;
  }
}
