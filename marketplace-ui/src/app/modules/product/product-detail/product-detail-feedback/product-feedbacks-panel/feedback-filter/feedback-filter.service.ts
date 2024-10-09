import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { ItemDropdown } from '../../../../../../shared/models/item-dropdown.model';
import { FeedbackSortType } from '../../../../../../shared/enums/feedback-sort-type';

@Injectable({
  providedIn: 'root'
})
export class FeedbackFilterService {
  private readonly sortBySubject = new Subject<ItemDropdown<FeedbackSortType>>();
  data: ItemDropdown<FeedbackSortType> | undefined;
  event$ = this.sortBySubject.asObservable();

  changeSortByLabel(data: ItemDropdown<FeedbackSortType>) {
    this.data = data;
    this.sortBySubject.next(data);
  }
}
