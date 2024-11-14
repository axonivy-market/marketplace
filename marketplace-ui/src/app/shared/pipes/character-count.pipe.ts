import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Feedback } from '../models/feedback.model';
import { MAX_FEEDBACK_LENGTH } from '../constants/common.constant';

@Pipe({
  standalone: true,
  name: 'characterCount'
})
export class CharacterCountPipe implements PipeTransform {
  translateService = inject(TranslateService);
  transform(feedback: Feedback): string {
    console.log('ndkhanh');
    
    return `${feedback.content.length}/${MAX_FEEDBACK_LENGTH}`;
  }
}
