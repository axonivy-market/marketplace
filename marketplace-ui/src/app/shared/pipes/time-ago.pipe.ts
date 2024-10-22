import { inject, Pipe, PipeTransform } from '@angular/core';
import { Language } from '../enums/language.enum';
import { TranslateService } from '@ngx-translate/core';
import { TimeAgo } from '../enums/time-ago.enum';
import { DAYS_IN_A_MONTH, DAYS_IN_A_WEEK, DAYS_IN_A_YEAR, HOURS_IN_A_DAY, MINUTES_IN_A_HOUR, SECONDS_IN_A_MINUTE } from '../constants/common.constant';

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
      timeAgo = `${years} ${this.translateService.instant(TimeAgo.YEARS_AGO)}`;
    } else if (years === 1) {
      timeAgo = `${years} ${this.translateService.instant(TimeAgo.YEAR_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getMonthsAgoText(months: number) {
    let timeAgo;
    if (months > 1) {
      timeAgo = `${months} ${this.translateService.instant(TimeAgo.MONTHS_AGO)}`;
    } else if (months === 1) {
      timeAgo = `${months} ${this.translateService.instant(TimeAgo.MONTH_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getWeeksAgoText(weeks: number) {
    let timeAgo;
    if (weeks > 1) {
      timeAgo = `${weeks} ${this.translateService.instant(TimeAgo.WEEKS_AGO)}`;
    } else if (weeks === 1) {
      timeAgo = `${weeks} ${this.translateService.instant(TimeAgo.WEEK_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getDaysAgoText(days: number) {
    let timeAgo;
    if (days > 1) {
      timeAgo = `${days} ${this.translateService.instant(TimeAgo.DAYS_AGO)}`;
    } else if (days === 1) {
      timeAgo = `${days} ${this.translateService.instant(TimeAgo.DAY_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getHoursAgoText(hours: number) {
    let timeAgo;
    if (hours > 1) {
      timeAgo = `${hours} ${this.translateService.instant(TimeAgo.HOURS_AGO)}`;
    } else if (hours === 1) {
      timeAgo = `${hours} ${this.translateService.instant(TimeAgo.HOUR_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getMinutesAgoText(minutes: number) {
    let timeAgo;
    if (minutes > 1) {
      timeAgo = `${minutes} ${this.translateService.instant(TimeAgo.MINUTES_AGO)}`;
    } else if (minutes === 1) {
      timeAgo = `${minutes} ${this.translateService.instant(TimeAgo.MINUTE_AGO)}`;
    } else {
      timeAgo = ''
    }

    return timeAgo;
  }

  private getSecondsAgoText(seconds: number) {
    let timeAgo;
    if (seconds > 1) {
      timeAgo = `${seconds} ${this.translateService.instant(TimeAgo.SECONDS_AGO)}`;
    } else {
      timeAgo = `${seconds} ${this.translateService.instant(TimeAgo.SECOND_AGO)}`;
    }

    return timeAgo;
  }
}
