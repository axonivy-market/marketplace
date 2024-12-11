import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private readonly loadingSubject = new BehaviorSubject<{
    [key: string]: boolean;
  }>({});
  loading$ = this.loadingSubject.asObservable;

  private setLoading(componentIds: string[], isLoading: boolean): void {
    const currentState = this.loadingSubject.value;

    // Update the loading state for each component ID
    componentIds.forEach(id => {
      currentState[id] = isLoading;
    });

    this.loadingSubject.next({ ...currentState });
  }

  showLoading(...componentIds: string[]): void {
    this.setLoading(componentIds, true);
  }

  hideLoading(...componentIds: string[]) {
    this.setLoading(componentIds, false);
  }

  isLoading(compnentId: string): boolean {
    return this.loadingSubject.value[compnentId] || false;
  }
}
