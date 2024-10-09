import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FeedbackFilterService {
  private sortBySubject = new Subject<any>();
  data: any = undefined;
  event$ = this.sortBySubject.asObservable();

  changeSortByLabel(data: any) {
    this.data = data;
    this.sortBySubject.next(data);
  }
}
