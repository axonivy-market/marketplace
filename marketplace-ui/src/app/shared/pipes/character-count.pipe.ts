import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MAX_FEEDBACK_LENGTH } from '../constants/common.constant';

@Pipe({
  standalone: true,
  name: 'characterCount'
})
export class CharacterCountPipe implements PipeTransform {
  translateService = inject(TranslateService);
  transform(value: string): string {   
    return `${value.length}/${MAX_FEEDBACK_LENGTH}`;
  }
}
