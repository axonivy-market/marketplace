import { TestBed } from '@angular/core/testing';
import {
  MissingTranslationHandler,
  TranslateLoader,
  TranslateModule,
  TranslateService
} from '@ngx-translate/core';
import { Language } from '../enums/language.enum';
import { TimeAgo } from '../enums/time-ago.enum';
import { TimeAgoPipe } from './time-ago.pipe';
import { httpLoaderFactory } from '../../core/configs/translate.config';

describe('TimeAgoPipe', () => {
  let pipe: TimeAgoPipe;
  let translateService: TranslateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useFactory: httpLoaderFactory
          },
          missingTranslationHandler: {
            provide: MissingTranslationHandler,
            useValue: { handle: () => 'Translation missing' }
          }
        })
      ],
      providers: [TimeAgoPipe]
    });

    translateService = TestBed.inject(TranslateService);
    pipe = TestBed.inject(TimeAgoPipe);
  });

  it('should render the text 1 year ago', () => {
    const oneYearAgo = new Date();
    oneYearAgo.setFullYear(new Date().getFullYear() - 1);
    expect(pipe.transform(oneYearAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.YEAR_AGO, { number: 1 })
    );
  });

  it('should render the text 2 years ago', () => {
    const twoYearsAgo = new Date();
    twoYearsAgo.setFullYear(new Date().getFullYear() - 2);
    expect(pipe.transform(twoYearsAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.YEARS_AGO, { number: 2 })
    );
  });

  it('should render the text 1 month ago', () => {
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(new Date().getMonth() - 1);
    expect(pipe.transform(oneMonthAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.MONTH_AGO, { number: 1 })
    );
  });

  it('should render the text 2 months ago', () => {
    const twoMonthsAgo = new Date();
    twoMonthsAgo.setMonth(new Date().getMonth() - 2);
    expect(pipe.transform(twoMonthsAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.MONTHS_AGO, { number: 2 })
    );
  });

  it('should render the text 1 week ago', () => {
    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(new Date().getDate() - 7);
    expect(pipe.transform(oneWeekAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.WEEK_AGO, { number: 1 })
    );
  });

  it('should render the text 2 weeks ago', () => {
    const twoWeeksAgo = new Date();
    twoWeeksAgo.setDate(new Date().getDate() - 14);
    expect(pipe.transform(twoWeeksAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.WEEKS_AGO, { number: 2 })
    );
  });

  it('should render the text 1 day ago', () => {
    const oneDayAgo = new Date();
    oneDayAgo.setDate(new Date().getDate() - 1);
    expect(pipe.transform(oneDayAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.DAY_AGO, { number: 1 })
    );
  });

  it('should render the text 2 days ago', () => {
    const twoDaysAgo = new Date();
    twoDaysAgo.setDate(new Date().getDate() - 2);
    expect(pipe.transform(twoDaysAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.DAYS_AGO, { number: 2 })
    );
  });

  it('should render the text 1 hour ago', () => {
    const oneHourAgo = new Date();
    oneHourAgo.setHours(new Date().getHours() - 1);
    expect(pipe.transform(oneHourAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.HOUR_AGO, { number: 1 })
    );
  });

  it('should render the text 2 hours ago', () => {
    const twoHoursAgo = new Date();
    twoHoursAgo.setHours(new Date().getHours() - 2);
    expect(pipe.transform(twoHoursAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.HOURS_AGO, { number: 2 })
    );
  });

  it('should render the text 1 minute ago', () => {
    const oneMinuteAgo = new Date();
    oneMinuteAgo.setMinutes(new Date().getMinutes() - 1);
    expect(pipe.transform(oneMinuteAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.MINUTE_AGO, { number: 1 })
    );
  });

  it('should render the text 2 minutes ago', () => {
    const twoMinutesAgo = new Date();
    twoMinutesAgo.setMinutes(new Date().getMinutes() - 2);
    expect(pipe.transform(twoMinutesAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.MINUTES_AGO, { number: 2 })
    );
  });

  it('should render the text 0 second ago', () => {
    expect(pipe.transform(new Date(), Language.EN)).toBe(
      translateService.instant(TimeAgo.SECOND_AGO, { number: 0 })
    );
  });

  it('should render the text 2 seconds ago', () => {
    const twoSecondsAgo = new Date();
    twoSecondsAgo.setSeconds(new Date().getSeconds() - 2);
    expect(pipe.transform(twoSecondsAgo, Language.EN)).toBe(
      translateService.instant(TimeAgo.SECONDS_AGO, { number: 2 })
    );
  });
});
