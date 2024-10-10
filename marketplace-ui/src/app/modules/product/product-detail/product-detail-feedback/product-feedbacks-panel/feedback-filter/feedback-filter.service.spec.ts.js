import { TestBed } from '@angular/core/testing';
import { FeedbackFilterService } from './feedback-filter.service';

describe('FeedbackFilterService', () => {
  let service: FeedbackFilterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FeedbackFilterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have undefined data initially', () => {
    expect(service.data).toBeUndefined();
  });

  it('should update data and emit the value through event$', () => {
    const mockData = { value: 'test', label: 'Test Label' };

    // Spy on the next method of the Subject (sortBySubject)
    spyOn(service['sortBySubject'], 'next').and.callThrough();

    // Subscribe to the event$ observable to listen for changes
    let emittedValue: any;
    service.event$.subscribe(value => emittedValue = value);

    // Call the changeSortByLabel function
    service.changeSortByLabel(mockData);

    // Expect the data to be updated
    expect(service.data).toEqual(mockData);

    // Expect the next method to have been called with the correct value
    expect(service['sortBySubject'].next).toHaveBeenCalledWith(mockData);

    // Expect the emitted value to match the mockData
    expect(emittedValue).toEqual(mockData);
  });
});
