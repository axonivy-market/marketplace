import { Language } from "../enums/language.enum";
import { SortOption } from "../enums/sort-option.enum";
import { TypeOption } from "../enums/type-option.enum";
export interface Criteria {
  search: string;
  sort: SortOption | null;
  type: TypeOption | null;
  language: Language;
  nextPageHref?: string;
}
