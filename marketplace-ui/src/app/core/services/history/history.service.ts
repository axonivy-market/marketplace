import { Injectable, signal, WritableSignal } from '@angular/core';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { FILTER_TYPES } from '../../../shared/constants/common.constant';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  lastSearchText: WritableSignal<string> = signal('');
  lastSortOption: WritableSignal<SortOption> = signal(SortOption.STANDARD);
  lastSearchType: WritableSignal<string> = signal(FILTER_TYPES[0].value);

  isLastSearchChanged(): boolean {
    return (
      this.lastSearchText() !== '' ||
      this.lastSortOption() !== SortOption.STANDARD ||
      this.lastSearchType() !== FILTER_TYPES[0].value
    );
  }
}