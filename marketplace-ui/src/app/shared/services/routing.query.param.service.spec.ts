import { TestBed } from '@angular/core/testing';
import { Router, NavigationStart } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { RoutingQueryParamService } from './routing.query.param.service';
import { Subject } from 'rxjs';
import { DESIGNER_SESSION_STORAGE_VARIABLE } from '../constants/common.constant';

describe('RoutingQueryParamService', () => {
  let service: RoutingQueryParamService;
  let cookieService: jasmine.SpyObj<CookieService>;
  let eventsSubject: Subject<NavigationStart>;

  beforeEach(() => {
    const cookieServiceSpy = jasmine.createSpyObj('CookieService', [
      'get',
      'set'
    ]);
    eventsSubject = new Subject<NavigationStart>();
    const routerSpy = jasmine.createSpyObj('Router', [], {
      events: eventsSubject.asObservable()
    });

    TestBed.configureTestingModule({
      providers: [
        RoutingQueryParamService,
        { provide: CookieService, useValue: cookieServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
    service = TestBed.inject(RoutingQueryParamService);
    cookieService = TestBed.inject(
      CookieService
    ) as jasmine.SpyObj<CookieService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check session storage for designer version', () => {
    const params = {
      [DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName]: '1.0'
    };
    service.checkSessionStorageForDesignerVersion(params);
    expect(cookieService.set).toHaveBeenCalledWith(
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
    expect(cookieService.set).toHaveBeenCalledWith(
      DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName,
      DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    );
    expect(service.isDesignerViewer()).toBeTrue();
  });

  it('should get designer version from session storage if not set', () => {
    cookieService.get.and.returnValue('1.0');
    expect(service.getDesignerVersionFromSessionStorage()).toBe('1.0');
  });

  it('should set isDesigner to true if session storage matches default designer viewer', () => {
    cookieService.get.and.returnValue(
      DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    );
    expect(service.isDesignerViewer()).toBeTrue();
  });

  it('should listen to navigation start events', () => {
    cookieService.get.and.returnValue(
      DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    );
    eventsSubject.next(new NavigationStart(1, 'testUrl'));
    expect(service.isDesignerViewer()).toBeTrue();
  });
});
