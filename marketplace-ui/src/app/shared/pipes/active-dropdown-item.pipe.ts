import { inject, Pipe, PipeTransform } from "@angular/core";
import { ItemDropdown } from "../models/item-dropdown.model";
import { TranslateService } from "@ngx-translate/core";

@Pipe({
  standalone: true,
  name: 'activeItemPipe'
})
export class ActiveDropDownItemPipe implements PipeTransform {
  translateService = inject(TranslateService);

  transform(value: ItemDropdown, selectedItem: Object | undefined): boolean {
    return this.translateService.instant(value.label) === selectedItem;
  }
}