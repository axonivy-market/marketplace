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
import { MarkdownService } from '../../shared/services/markdown.service';

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

    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBeFalse();
  });

  it('should replace existing file and reset uploaded state', () => {
    const first = new File(['one'], 'one.zip', { type: 'application/zip' });
    const second = new File(['two'], 'two.zip', { type: 'application/zip' });

    component.onFileSelected({ target: { files: [first] } } as unknown as Event);
    component.isUploaded = true;

    component.onFileSelected({ target: { files: [second] } } as unknown as Event);
    expect(component.selectedFile).toEqual(second);
    expect(component.isUploaded).toBeFalse();
  });

  it('should remove file when removeFile called', () => {
    const mockFile = new File(['content'], 'test.zip', { type: 'application/zip' });
    component.onFileSelected({ target: { files: [mockFile] } } as unknown as Event);
    component.removeFile();

    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBeFalse();
  });

  it('should handle drag over and leave toggling isDragging', () => {
    const dragEventOver = new DragEvent('dragover');
    component.onDragOver(dragEventOver);
    expect(component.isDragging).toBeTrue();

    const dragEventLeave = new DragEvent('dragleave');
    component.onDragLeave(dragEventLeave);
    expect(component.isDragging).toBeFalse();
  });

  it('should accept dropped zip file', () => {
    const mockFile = new File(['zip'], 'drop.zip', { type: 'application/zip' });
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(mockFile);

    const dropEvent = new DragEvent('drop', { dataTransfer });
    component.onDrop(dropEvent);

    expect(component.selectedFile).toEqual(mockFile);
    expect(component.isZipFile).toBeTrue();
  });

  it('should reject dropped non-zip file', () => {
    const mockFile = new File(['nope'], 'nope.txt', { type: 'text/plain' });
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(mockFile);

    const dropEvent = new DragEvent('drop', { dataTransfer });
    component.onDrop(dropEvent);

    expect(component.selectedFile).toBeNull();
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
    const mockResponse = MOCK_RELEASE_PREVIEW_DATA;
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

  it('should render and sanitize readme content', () => {
    const markdownService = TestBed.inject(MarkdownService);
    spyOn(markdownService, 'parseMarkdown').and.callThrough();
    sanitizerSpy.sanitize.and.callFake((_ctx, html) => html as string);
    languageService.selectedLanguage.and.returnValue(Language.EN);
    spyOn(releasePreviewService, 'extractZipDetails').and.returnValue(
      of({
        description: {
          en: '<details><summary>More description</summary><strong>This is a test</strong></details>'
        },
        setup: {
          en: '<details><summary>How to setup</summary><p>This is a setup test</p></details>'
        },
        demo: {}
      })
    );

    component.selectedFile = new File(['zip'], 'asana-connector-product.zip', {
      type: 'application/zip'
    });
    component.isZipFile = true;

    component.handlePreviewPage();

    const descriptionHtml = component.loadedReadmeContent[
      'description'
    ] as unknown as string;
    expect(descriptionHtml).toContain('<details>');
    expect(descriptionHtml).toContain('<summary>More description</summary>');
    expect(descriptionHtml).toContain('<strong>This is a test</strong>');
  });

  it('should clear errorMessage for a valid zip within size', () => {
    const validZip: Partial<File> = {
      name: 'valid.zip',
      type: 'application/zip',
      size: 5 * 1024 * 1024
    };
    component.setSelectedFile(validZip as File);

    expect(component.errorMessage).toBeNull();
    expect(component.selectedFile?.name).toBe('valid.zip');
    expect(component.isZipFile).toBeTrue();
  });

  it('should set invalidZip error for non-zip file', () => {
    const invalidType: Partial<File> = {
      name: 'note.txt',
      type: 'text/plain',
      size: 10 * 1024
    };
    component.setSelectedFile(invalidType as File);

    expect(component.errorMessage).toBe('common.preview.errors.invalidZip');
    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBeFalse();
  });

  it('should set tooLarge error for oversize zip file', () => {
    const oversizeZip: Partial<File> = {
      name: 'huge.zip',
      type: 'application/zip',
      size: 21 * 1024 * 1024
    };
    component.setSelectedFile(oversizeZip as File);

    expect(component.errorMessage).toBe('common.preview.errors.tooLarge');
    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBeFalse();
  });

  it('should clear previous error when a new valid zip is selected', () => {
    const invalidType: Partial<File> = {
      name: 'bad.txt',
      type: 'text/plain',
      size: 100
    };
    component.setSelectedFile(invalidType as File);

    expect(component.errorMessage).toBe('common.preview.errors.invalidZip');

    const validZip: Partial<File> = {
      name: 'fixed.zip',
      type: 'application/zip',
      size: 1 * 1024 * 1024
    };
    component.setSelectedFile(validZip as File);

    expect(component.errorMessage).toBeNull();
    expect(component.selectedFile?.name).toBe('fixed.zip');
    expect(component.isZipFile).toBeTrue();
  });
});
