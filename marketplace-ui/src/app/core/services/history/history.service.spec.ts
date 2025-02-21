import { TestBed } from '@angular/core/testing';
import { HistoryService } from './history.service';
import { FILTER_TYPES } from '../../../shared/constants/common.constant';
import { SortOption } from '../../../shared/enums/sort-option.enum';

describe('LanguageService', () => {
  let service: HistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistoryService);
  });

  it('should initialize with default values', () => {
    expect(service.lastSearchText()).toBe('');
    expect(service.lastSortOption()).toBe(SortOption.STANDARD);
    expect(service.lastSearchType()).toBe(FILTER_TYPES[0].value);
  });

  it('should return false when no changes have been made', () => {
    expect(service.isLastSearchChanged()).toBeFalse();
  });

  it('should return true if lastSearchText changes', () => {
    service.lastSearchText.set('test search');
    expect(service.isLastSearchChanged()).toBeTrue();
  });


  it('should return true if lastSearchType changes', () => {
    service.lastSearchType.set('differentType');
    expect(service.isLastSearchChanged()).toBeTrue();
  });


  it('should return false after resetting values to defaults', () => {
    service.lastSearchText.set('changed');
    service.lastSortOption.set(SortOption.ALPHABETICALLY);
    service.lastSearchType.set('anotherType');

    service.lastSearchText.set('');
    service.lastSortOption.set(SortOption.STANDARD);
    service.lastSearchType.set(FILTER_TYPES[0].value);

    expect(service.isLastSearchChanged()).toBeFalse();
  });
});