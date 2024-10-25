import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import {
  DAYS_IN_A_MONTH,
  DAYS_IN_A_WEEK,
  DAYS_IN_A_YEAR,
  HOURS_IN_A_DAY,
  MINUTES_IN_A_HOUR,
  SECONDS_IN_A_MINUTE
} from '../constants/common.constant';
import { Language } from '../enums/language.enum';
import { TimeAgo } from '../enums/time-ago.enum';

@Pipe({
  standalone: true,
  name: 'timeAgo'
})
export class TimeAgoPipe implements PipeTransform {
  translateService = inject(TranslateService);
  async transform(value?: Date, language?: Language, _args?: []): Promise<string> {
    if (value === undefined || language === undefined) {
      return '';
    }

    this.translateService.setDefaultLang(language);
    await firstValueFrom(this.translateService.use(language));
    return this.getTimeAgoValue(value);
  }

  getTimeAgoValue(value: Date): string {
    const date = new Date(value);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const seconds = Math.floor(diff / 1000);

    const minutes = Math.floor(seconds / SECONDS_IN_A_MINUTE);
    const hours = Math.floor(minutes / MINUTES_IN_A_HOUR);
    const days = Math.floor(hours / HOURS_IN_A_DAY);
    const weeks = Math.floor(days / DAYS_IN_A_WEEK);
    const months = Math.floor(days / DAYS_IN_A_MONTH);
    const years = Math.floor(days / DAYS_IN_A_YEAR);

    const timeIntervals = [
      { value: years, singularKey: TimeAgo.YEAR_AGO, pluralKey: TimeAgo.YEARS_AGO },
      { value: months, singularKey: TimeAgo.MONTH_AGO, pluralKey: TimeAgo.MONTHS_AGO },
      { value: weeks, singularKey: TimeAgo.WEEK_AGO, pluralKey: TimeAgo.WEEKS_AGO },
      { value: days, singularKey: TimeAgo.DAY_AGO, pluralKey: TimeAgo.DAYS_AGO },
      { value: hours, singularKey: TimeAgo.HOUR_AGO, pluralKey: TimeAgo.HOURS_AGO },
      { value: minutes, singularKey: TimeAgo.MINUTE_AGO, pluralKey: TimeAgo.MINUTES_AGO },
      { value: seconds, singularKey: TimeAgo.SECOND_AGO, pluralKey: TimeAgo.SECONDS_AGO }
    ];
    let timeValue;
    for (const timeInterval of timeIntervals) {
      if (timeInterval.value > 1) {
        timeValue = this.getTranslatedTimeAgo(
          timeInterval.pluralKey,
          timeInterval.value
        );
      } else if (timeInterval.value === 1) {
        timeValue = this.getTranslatedTimeAgo(timeInterval.singularKey);
      } else {
        timeValue = this.getTranslatedTimeAgo(TimeAgo.SECOND_AGO);
      }
    }
    return timeValue;
  }

  private getTranslatedTimeAgo(timeAgo: TimeAgo, timeNumber?: number) {
    if (timeNumber === undefined) {
      return this.translateService.instant(timeAgo);
    }
    return this.translateService.instant(timeAgo, { number: timeNumber });
  }
}
