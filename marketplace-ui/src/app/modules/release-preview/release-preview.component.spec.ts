import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReleasePreviewComponent } from './release-preview.component';
import { ReleasePreviewService } from './release-preview.service';
import { of, throwError } from 'rxjs';
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
import { ItemDropdown } from '../../shared/models/item-dropdown.model';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';

describe('ReleasePreviewComponent', () => {
  let component: ReleasePreviewComponent;
  let fixture: ComponentFixture<ReleasePreviewComponent>;
  let releasePreviewService: ReleasePreviewService;
  let languageService: MockedObject<LanguageService>;
  let sanitizerSpy: MockedObject<DomSanitizer>;
  const spy = {
    bypassSecurityTrustHtml: vi
      .fn()
      .mockName('DomSanitizer.bypassSecurityTrustHtml'),
    sanitize: vi.fn().mockName('DomSanitizer.sanitize')
  };

  beforeEach(async () => {
    const languageServiceSpy = {
      selectedLanguage: vi.fn().mockName('LanguageService.selectedLanguage')
    };

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
    ) as MockedObject<LanguageService>;
    sanitizerSpy = TestBed.inject(DomSanitizer) as MockedObject<DomSanitizer>;
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
    expect(component.isZipFile).toBe(true);
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
    expect(component.isZipFile).toBe(false);
  });

  it('should replace existing file and reset uploaded state', () => {
    const first = new File(['one'], 'one.zip', { type: 'application/zip' });
    const second = new File(['two'], 'two.zip', { type: 'application/zip' });

    component.onFileSelected({
      target: { files: [first] }
    } as unknown as Event);
    component.isUploaded = true;

    component.onFileSelected({
      target: { files: [second] }
    } as unknown as Event);
    expect(component.selectedFile).toEqual(second);
    expect(component.isUploaded).toBe(false);
  });

  it('should remove file when removeFile called', () => {
    const mockFile = new File(['content'], 'test.zip', {
      type: 'application/zip'
    });
    component.onFileSelected({
      target: { files: [mockFile] }
    } as unknown as Event);
    component.removeFile();

    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBe(false);
  });

  it('should handle drag over and leave toggling isDragging', () => {
    const dragEventOver = new DragEvent('dragover');
    component.onDragOver(dragEventOver);
    expect(component.isDragging).toBe(true);

    const dragEventLeave = new DragEvent('dragleave');
    component.onDragLeave(dragEventLeave);
    expect(component.isDragging).toBe(false);
  });

  it('should accept dropped zip file', () => {
    const mockFile = new File(['zip'], 'drop.zip', { type: 'application/zip' });
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(mockFile);

    const dropEvent = new DragEvent('drop', { dataTransfer });
    component.onDrop(dropEvent);

    expect(component.selectedFile).toEqual(mockFile);
    expect(component.isZipFile).toBe(true);
  });

  it('should reject dropped non-zip file', () => {
    const mockFile = new File(['nope'], 'nope.txt', { type: 'text/plain' });
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(mockFile);

    const dropEvent = new DragEvent('drop', { dataTransfer });
    component.onDrop(dropEvent);

    expect(component.selectedFile).toBeNull();
    expect(component.isZipFile).toBe(false);
  });

  it('should handle file upload and call service', () => {
    vi.spyOn(releasePreviewService, 'extractZipDetails');

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
    vi.spyOn(component, 'getContent').mockImplementation(
      tab => tab === 'description'
    );

    const displayedTabs = component.getDisplayedTabsSignal();

    expect(displayedTabs.length).toBe(1);
    expect(displayedTabs[0].value).toBe('description');
  });

  it('should return true for description when in DE language it is not null and not undefined and not empty', () => {
    component.readmeContent.set({
      description: { en: 'Description content' },
      setup: {},
      demo: {},
      component: {}
    });

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    expect(component.getContent('description')).toBe(true);
    expect(component.getContent('setup')).toBe(false);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should handle successful file upload', () => {
    const mockResponse = MOCK_RELEASE_PREVIEW_DATA;
    vi.spyOn(releasePreviewService, 'extractZipDetails').mockReturnValue(
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
    vi.spyOn(markdownService, 'parseMarkdown');
    sanitizerSpy.sanitize.mockImplementation((_ctx, html) => html as string);
    languageService.selectedLanguage.mockReturnValue(Language.EN);
    vi.spyOn(releasePreviewService, 'extractZipDetails').mockReturnValue(
      of({
        description: {
          en: '<details><summary>More description</summary><strong>This is a test</strong></details>'
        },
        setup: {
          en: '<details><summary>How to setup</summary><p>This is a setup test</p></details>'
        },
        demo: {},
        component: {}
      })
    );

    component.selectedFile = new File(['zip'], 'asana-connector-product.zip', {
      type: 'application/zip'
    });
    component.isZipFile = true;

    component.handlePreviewPage();

    const descriptionHtml = component.loadedReadmeContent()['description'] as unknown as string;
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
    expect(component.isZipFile).toBe(true);
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
    expect(component.isZipFile).toBe(false);
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
    expect(component.isZipFile).toBe(false);
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
    expect(component.isZipFile).toBe(true);
  });

  it('should toggle shouldShowHint', () => {
    expect(component.shouldShowHint).toBe(false);

    component.toggleHint();
    expect(component.shouldShowHint).toBe(true);

    component.toggleHint();
    expect(component.shouldShowHint).toBe(false);
  });

  it('should convert bytes to MB string with 2 decimals', () => {
    const bytes = 1048576; // 1MB
    const result = component.fileSizeInMB(bytes);

    expect(result).toBe('1.00');
  });

  it('should not call service if no selectedFile', () => {
    vi.spyOn(releasePreviewService, 'extractZipDetails');

    component.selectedFile = null;
    component.isZipFile = false;
    component.onSubmit();

    expect(releasePreviewService.extractZipDetails).not.toHaveBeenCalled();
  });

  it('should not call service if selectedFile is not zip', () => {
    vi.spyOn(releasePreviewService, 'extractZipDetails');

    component.selectedFile = {} as File;
    component.isZipFile = false;
    component.onSubmit();

    expect(releasePreviewService.extractZipDetails).not.toHaveBeenCalled();
  });

  it('should handle service error and set errorMessage', () => {
    vi.spyOn(releasePreviewService, 'extractZipDetails').mockReturnValue(
      throwError(() => ({
        error: { message: 'upload.failed' }
      }))
    );

    component.selectedFile = new File(['zip'], 'bad.zip', {
      type: 'application/zip'
    });
    component.isZipFile = true;

    component.handlePreviewPage();

    expect(component.isUploaded).toBe(true);
    expect(component.errorMessage).toBe('upload.failed');
  });

  it('should return label for active tab', () => {
    component.activeTab = 'description';
    const label = component.getSelectedTabLabel();

    expect(label).toBeDefined();
  });

  it('should return null when readme content is missing', () => {
    component.readmeContent.set({} as ReleasePreviewData);
    const result = component.getReadmeContentValue({
      value: 'description'
    } as ItemDropdown);

    expect(result).toBeNull();
  });
});
