import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReleasePreviewComponent } from './release-preview.component';
import { ReleasePreviewService } from './release-preview.service';
import { of } from 'rxjs';
import { LanguageService } from '../../core/services/language/language.service';
import { Language } from '../../shared/enums/language.enum';
import { TranslateModule } from '@ngx-translate/core';
import { DomSanitizer } from '@angular/platform-browser';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MOCK_RELEASE_PREVIEW_DATA } from '../../shared/mocks/mock-data';

describe('ReleasePreviewComponent', () => {
  let component: ReleasePreviewComponent;
  let fixture: ComponentFixture<ReleasePreviewComponent>;
  let releasePreviewService: ReleasePreviewService;
  let languageService: jasmine.SpyObj<LanguageService>;
  let sanitizerSpy: jasmine.SpyObj<DomSanitizer>;
  const spy = jasmine.createSpyObj('DomSanitizer', [
    'bypassSecurityTrustHtml',
    'sanitize'
  ]);

  beforeEach(async () => {
    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'selectedLanguage'
    ]);

    await TestBed.configureTestingModule({
      imports: [ReleasePreviewComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        },
        { provide: DomSanitizer, useValue: spy }
      ]
    }).compileComponents();
    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;
    sanitizerSpy = TestBed.inject(DomSanitizer) as jasmine.SpyObj<DomSanitizer>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReleasePreviewComponent);
    component = fixture.componentInstance;
    releasePreviewService = TestBed.inject(ReleasePreviewService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selected file on file selection', () => {
    const mockFile = new File(['content'], 'test.zip', {
      type: 'application/zip'
    });
    const event = {
      target: {
        files: [mockFile]
      }
    } as unknown as Event;

    component.onFileSelected(event);

    expect(component.selectedFile).toEqual(mockFile);
    expect(component.isZipFile).toBeTrue();
  });

  it('should check non-zip file', () => {
    const mockFile = new File(['content'], 'test.txt', { type: 'text/plain' });
    const event = {
      target: {
        files: [mockFile]
      }
    } as unknown as Event;

    component.onFileSelected(event);

    expect(component.selectedFile).toEqual(mockFile);
    expect(component.isZipFile).toBeFalse();
  });

  it('should handle file upload and call service', () => {
    spyOn(releasePreviewService, 'extractZipDetails').and.callThrough();

    const mockFile = new File(['content'], 'test.zip', {
      type: 'application/zip'
    });
    component.selectedFile = mockFile;
    component.isZipFile = true;

    component.onSubmit();

    expect(releasePreviewService.extractZipDetails).toHaveBeenCalledWith(
      mockFile
    );
  });

  it('should filter tabs based on available content', () => {
    spyOn(component, 'getContent').and.callFake(tab => tab === 'description');

    const displayedTabs = component.getDisplayedTabsSignal();

    expect(displayedTabs.length).toBe(1);
    expect(displayedTabs[0].value).toBe('description');
  });

  it('should return true for description when in DE language it is not null and not undefined and not empty', () => {
    component.readmeContent.set({
      description: { en: 'Description content' },
      setup: {},
      demo: {}
    });

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    expect(component.getContent('description')).toBeTrue();
    expect(component.getContent('setup')).toBeFalse();
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should handle successful file upload', () => {
    const mockResponse = MOCK_RELEASE_PREVIEW_DATA
    spyOn(releasePreviewService, 'extractZipDetails').and.returnValue(
      of(mockResponse)
    );

    component.selectedFile = new File(['content'], 'test.zip', {
      type: 'application/zip'
    });
    component.isZipFile = true;
    component.handlePreviewPage();

    expect(releasePreviewService.extractZipDetails).toHaveBeenCalledWith(
      component.selectedFile
    );
    expect(component.readmeContent()).toEqual(mockResponse);
  });

  it('should set activeTab when setActiveTab is called', () => {
    component.setActiveTab('setup');
    expect(component.activeTab).toBe('setup');
  });

  it('should render readme content as safe HTML', () => {
    const value = '**This is a test**';
    const mockedRenderedHtml = '<strong>This is a test</strong>';
    sanitizerSpy.bypassSecurityTrustHtml.and.returnValue(mockedRenderedHtml);
    const result = component.renderReadmeContent(value);
    expect(result).toBe(mockedRenderedHtml);
  });
});
