import { TestBed } from '@angular/core/testing';
import { Router, NavigationStart } from '@angular/router';
import { RoutingQueryParamService } from './routing.query.param.service';
import { Subject } from 'rxjs';
import { DESIGNER_SESSION_STORAGE_VARIABLE } from '../constants/common.constant';

describe('RoutingQueryParamService', () => {
  let service: RoutingQueryParamService;
  let eventsSubject: Subject<NavigationStart>;
  let mockStorage: { [key: string]: string };

  beforeEach(() => {
    eventsSubject = new Subject<NavigationStart>();
    const routerSpy = jasmine.createSpyObj('Router', [], {
      events: eventsSubject.asObservable()
    });

    TestBed.configureTestingModule({
      providers: [
        RoutingQueryParamService,
        { provide: Router, useValue: routerSpy }
      ]
    });
    service = TestBed.inject(RoutingQueryParamService);
    mockStorage = {
      'ivy-viewer': 'designer-market',
      'ivy-version': '1.0'
    };
    spyOn(sessionStorage, 'getItem').and.callFake((key: string) => {
      return mockStorage[key] || null;
    });

    spyOn(sessionStorage, 'setItem').and.callFake((key: string, value: string) => {
      mockStorage[key] = value;
    });
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check session storage for designer version', () => {
    const params = {
      [DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName]: '1.0'
    };
    service.checkSessionStorageForDesignerVersion(params);
    expect(sessionStorage.setItem).toHaveBeenCalledWith(
      DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName,
      '1.0'
    );
    expect(service.getDesignerVersionFromSessionStorage()).toBe('1.0');
  });

  it('should check session storage for designer env', () => {
    const params = {
      [DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName]:
        DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    };
    service.checkSessionStorageForDesignerEnv(params);
    expect(sessionStorage.setItem).toHaveBeenCalledWith(
      DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName,
      DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    );
    expect(service.isDesignerViewer()).toBeTrue();
  });

  it('should get designer version from session storage if not set', () => {
    expect(service.getDesignerVersionFromSessionStorage()).toBe('1.0');
  });

  it('should set isDesigner to true if session storage matches default designer viewer', () => {
    expect(service.isDesignerViewer()).toBeTrue();
  });

  it('should listen to navigation start events', () => {
    eventsSubject.next(new NavigationStart(1, 'testUrl'));
    expect(service.isDesignerViewer()).toBeTrue();
  });
});
