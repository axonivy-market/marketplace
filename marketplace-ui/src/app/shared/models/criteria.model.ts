import { Language } from '../enums/language.enum';
import { SortOption } from '../enums/sort-option.enum';
import { TypeOption } from '../enums/type-option.enum';
import { Pageable } from './apis/pageable.model';

export interface Criteria {
  search: string;
  sort: SortOption | null;
  type: TypeOption | null;
  language: Language;
  isRESTClientEditor: boolean;
  nextPageHref?: string;
  pageable: Pageable;
}
