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
import { MatomoTestingModule, provideMatomoTesting } from 'ngx-matomo-client/testing';
import { MatomoRouterModule } from 'ngx-matomo-client';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HomeComponent, 
        TranslateModule.forRoot(), 
        MatomoTestingModule.forRoot(),
        MatomoRouterModule.forRoot(),],
      providers: [
        provideMatomoTesting(),
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        TranslateService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
