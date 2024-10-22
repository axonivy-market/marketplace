import { inject, Pipe, PipeTransform } from '@angular/core';
import { Language } from '../enums/language.enum';
import { TranslateService } from '@ngx-translate/core';
import { TimeAgo } from '../enums/time-ago.enum';
import {
  DAYS_IN_A_MONTH,
  DAYS_IN_A_WEEK,
  DAYS_IN_A_YEAR,
  HOURS_IN_A_DAY,
  MINUTES_IN_A_HOUR,
  SECONDS_IN_A_MINUTE
} from '../constants/common.constant';

@Pipe({
  standalone: true,
  name: 'timeAgo'
})
export class TimeAgoPipe implements PipeTransform {
  translateService = inject(TranslateService);
  transform(value?: Date, language?: Language, _args?: []): string {
    if (value === undefined || language === null) {
      return '';
    }

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

    const yearAgo = this.getYearsAgoText(years);
    if (yearAgo !== '') {
      return yearAgo;
    }

    const monthAgo = this.getMonthsAgoText(months);
    if (monthAgo !== '') {
      return monthAgo;
    }

    const weekAgo = this.getWeeksAgoText(weeks);
    if (weekAgo !== '') {
      return weekAgo;
    }

    const dayAgo = this.getDaysAgoText(days);
    if (dayAgo !== '') {
      return dayAgo;
    }

    const hourAgo = this.getHoursAgoText(hours);
    if (hourAgo !== '') {
      return hourAgo;
    }

    const minuteAgo = this.getMinutesAgoText(minutes);
    if (minuteAgo !== '') {
      return minuteAgo;
    }

    return this.getSecondsAgoText(seconds);
  }

  private getYearsAgoText(years: number) {
    let timeAgo;
    if (years > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.YEARS_AGO, years);
    } else if (years === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.YEAR_AGO, years);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getMonthsAgoText(months: number) {
    let timeAgo;
    if (months > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.MONTHS_AGO, months);
    } else if (months === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.MONTH_AGO, months);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getWeeksAgoText(weeks: number) {
    let timeAgo;
    if (weeks > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.WEEKS_AGO, weeks);
    } else if (weeks === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.WEEK_AGO, weeks);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getDaysAgoText(days: number) {
    let timeAgo;
    if (days > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.DAYS_AGO, days);
    } else if (days === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.DAY_AGO, days);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getHoursAgoText(hours: number) {
    let timeAgo;
    if (hours > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.HOURS_AGO, hours);
    } else if (hours === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.HOUR_AGO, hours);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getMinutesAgoText(minutes: number) {
    let timeAgo;
    if (minutes > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.MINUTES_AGO, minutes);
    } else if (minutes === 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.MINUTE_AGO, minutes);
    } else {
      timeAgo = '';
    }

    return timeAgo;
  }

  private getSecondsAgoText(seconds: number) {
    let timeAgo;
    if (seconds > 1) {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.SECONDS_AGO, seconds);
    } else {
      timeAgo = this.getTranslatedTimeAgo(TimeAgo.SECOND_AGO, seconds);
    }

    return timeAgo;
  }

  private getTranslatedTimeAgo(timeAgo: TimeAgo, timeNumber: number) {
    return this.translateService.instant(timeAgo, { number: timeNumber })
  }
}
