import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import {
  FILTER_TYPES,
  SORT_TYPES
} from '../../../shared/constants/common.constant';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import { SortOption } from '../../../shared/enums/sort-option.enum';

@Component({
  selector: 'app-product-filter',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './product-filter.component.html',
  styleUrl: './product-filter.component.scss'
})
export class ProductFilterComponent {
  @Output() searchChange = new EventEmitter<string>();
  @Output() filterChange = new EventEmitter<TypeOption>();
  @Output() sortChange = new EventEmitter<SortOption>();

  selectedType = TypeOption.All_TYPES;
  types = FILTER_TYPES;
  selectedSort: SortOption = SortOption.POPULARITY;
  sorts = SORT_TYPES;

  searchText = '';

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);

  onSelectType(type: TypeOption) {
    this.selectedType = type;
    this.filterChange.emit(type);
  }

  onSearchChanged(searchString: string) {
    this.searchChange.next(searchString);
  }

  onSortChange() {
    this.sortChange.next(this.selectedSort);
  }
}
