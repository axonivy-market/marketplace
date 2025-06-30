import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitoringRedirectComponent } from './monitoring-redirect.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MONITORING_REDIRECT_URL } from '../../constants/common.constant';


class MockTranslateService {
  get(key: string) {
    return {
      subscribe: (fn: (res: string) => void) => fn(key)
    };
  }
}

describe('MonitoringRedirectComponent', () => {
  let component: MonitoringRedirectComponent;
  let fixture: ComponentFixture<MonitoringRedirectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), MonitoringRedirectComponent],
      providers: [{ provide: TranslateService, useClass: MockTranslateService }]
    }).compileComponents();

    fixture = TestBed.createComponent(MonitoringRedirectComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to MONITERING_REDIRECT_URL on ngOnInit', () => {
    let redirectedUrl: string = '';

    const mockWindow: Pick<Window, 'location'> = {
      location: {
        set href(value: string) {
          redirectedUrl = value;
        }
      } as Location
    };
    (component as any).window = mockWindow;
    component.ngOnInit();
    expect(redirectedUrl).toBe(MONITORING_REDIRECT_URL);
  });
});