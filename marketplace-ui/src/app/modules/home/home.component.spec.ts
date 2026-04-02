import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import {
  FAVICON_DEFAULT_TYPE,
  FAVICON_DEFAULT_URL
} from './../../shared/constants/common.constant';
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

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let faviconServiceSpy: MockedObject<FaviconService>;

  beforeEach(async () => {
    faviconServiceSpy = {
      setFavicon: vi.fn().mockName('FaviconService.setFavicon')
    } as any;
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
    expect(faviconServiceSpy.setFavicon).toHaveBeenCalledTimes(1);
    expect(faviconServiceSpy.setFavicon).toHaveBeenCalledWith(
      FAVICON_DEFAULT_URL,
      FAVICON_DEFAULT_TYPE
    );
  });
});
