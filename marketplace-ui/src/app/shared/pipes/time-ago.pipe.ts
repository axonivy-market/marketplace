import { inject, Pipe, PipeTransform } from '@angular/core';
import { Language } from '../enums/language.enum';
import { TranslateService } from '@ngx-translate/core';
import { TimeAgo } from '../enums/time-ago.enum';

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
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);
    const months = Math.floor(days / 30);
    const years = Math.floor(days / 365);

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

    const secondAgo = this.getSecondsAgoText(seconds);
    if (secondAgo !== '') {
      return secondAgo;
    }

    return '';
  }

  private getYearsAgoText(years: number) {
    let timeAgo = '';
    if (years > 1) {
      timeAgo = `${years} ${this.translateService.instant(TimeAgo.YEARS_AGO)}`;
    } else if (years === 1) {
      timeAgo = `${years} ${this.translateService.instant(TimeAgo.YEAR_AGO)}`;
    }

    return timeAgo;
  }

  private getMonthsAgoText(months: number) {
    let timeAgo = '';
    if (months > 1) {
      timeAgo = `${months} ${this.translateService.instant(TimeAgo.MONTHS_AGO)}`;
    } else if (months === 1) {
      timeAgo = `${months} ${this.translateService.instant(TimeAgo.MONTH_AGO)}`;
    }

    return timeAgo;
  }

  private getWeeksAgoText(weeks: number) {
    let timeAgo = '';
    if (weeks > 1) {
      timeAgo = `${weeks} ${this.translateService.instant(TimeAgo.WEEKS_AGO)}`;
    } else if (weeks === 1) {
      timeAgo = `${weeks} ${this.translateService.instant(TimeAgo.WEEK_AGO)}`;
    }

    return timeAgo;
  }

  private getDaysAgoText(days: number) {
    let timeAgo = '';
    if (days > 1) {
      timeAgo = `${days} ${this.translateService.instant(TimeAgo.DAYS_AGO)}`;
    } else if (days === 1) {
      timeAgo = `${days} ${this.translateService.instant(TimeAgo.DAY_AGO)}`;
    }

    return timeAgo;
  }

  private getHoursAgoText(hours: number) {
    let timeAgo = '';
    if (hours > 1) {
      timeAgo = `${hours} ${this.translateService.instant(TimeAgo.HOURS_AGO)}`;
    } else if (hours === 1) {
      timeAgo = `${hours} ${this.translateService.instant(TimeAgo.HOUR_AGO)}`;
    }

    return timeAgo;
  }

  private getMinutesAgoText(minutes: number) {
    let timeAgo = '';
    if (minutes > 1) {
      timeAgo = `${minutes} ${this.translateService.instant(TimeAgo.MINUTES_AGO)}`;
    } else if (minutes === 1) {
      timeAgo = `${minutes} ${this.translateService.instant(TimeAgo.MINUTE_AGO)}`;
    }

    return timeAgo;
  }

  private getSecondsAgoText(seconds: number) {
    let timeAgo = '';
    if (seconds > 1) {
      timeAgo = `${seconds} ${this.translateService.instant(TimeAgo.SECONDS_AGO)}`;
    } else {
      timeAgo = `${seconds} ${this.translateService.instant(TimeAgo.SECOND_AGO)}`;
    }

    return timeAgo;
  }
}
