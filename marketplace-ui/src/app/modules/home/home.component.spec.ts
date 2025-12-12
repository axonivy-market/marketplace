import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { FaviconService } from '../../shared/services/favicon.service';
import { DEFAULT_FAVICON_URL } from '../../shared/constants/common.constant';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let faviconServiceSpy: jasmine.SpyObj<FaviconService>;

  beforeEach(async () => {
    faviconServiceSpy = jasmine.createSpyObj('FaviconService', ['setFavicon']);
    await TestBed.configureTestingModule({
      imports: [
        HomeComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        TranslateService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        { provide: FaviconService, useValue: faviconServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call faviconService.setFavicon on init', () => {
    expect(faviconServiceSpy.setFavicon).toHaveBeenCalledOnceWith(
      DEFAULT_FAVICON_URL
    );
  });
});
