import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';

vi.mock('bootstrap', () => ({
  Tooltip: vi.fn().mockImplementation(() => ({}))
}));
import { of } from 'rxjs';
import { ProductDetailVersionActionComponent } from './product-detail-version-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { ElementRef } from '@angular/core';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';
import { CookieService } from 'ngx-cookie-service';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { CommonUtils } from '../../../../shared/utils/common.utils';
import { ROUTER } from '../../../../shared/constants/router.constant';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { ProductDetailActionType } from '../../../../shared/enums/product-detail-action-type';
import { MATOMO_TRACKING_ENVIRONMENT } from '../../../../shared/constants/matomo.constant';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { environment } from '../../../../../environments/environment';
import { MavenArtifactKey } from '../../../../shared/models/maven-artifact.model';

class MockElementRef implements ElementRef {
  nativeElement = {
    contains: vi.fn().mockReturnValue(false),
    querySelector: vi.fn().mockReturnValue({ contains: vi.fn().mockReturnValue(false) })
  };
}

describe('ProductDetailVersionActionComponent', () => {
  const productId = '123';
  const _originalApiUrl = environment.apiUrl;
  let component: ProductDetailVersionActionComponent;
  let fixture: ComponentFixture<ProductDetailVersionActionComponent>;
  let productServiceMock: any;
  let router: Router;
  let route: MockedObject<ActivatedRoute>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    productServiceMock = {
      sendRequestToProductDetailVersionAPI: vi
        .fn()
        .mockName('ProductService.sendRequestToProductDetailVersionAPI'),
      sendRequestToGetInstallationCount: vi
        .fn()
        .mockName('ProductService.sendRequestToGetInstallationCount'),
      sendRequestToGetProductVersionsForDesigner: vi
        .fn()
        .mockName('ProductService.sendRequestToGetProductVersionsForDesigner')
    };
    const commonUtilsSpy = {
      getCookieValue: vi.fn().mockName('CommonUtils.getCookieValue')
    };
    const activatedRouteSpy = {
      snapshot: {
        queryParams: {},
        queryParamMap: {
          get: (key: string) => {
            if (key === ROUTER.VERSION) return '1.0';
            return null;
          }
        },
        fragment: 'description'
      }
    };

    TestBed.configureTestingModule({
      imports: [
        ProductDetailVersionActionComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        TranslateService,
        CookieService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ProductService, useValue: productServiceMock },
        { provide: ElementRef, useClass: MockElementRef },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        { provide: CommonUtils, useValue: commonUtilsSpy }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductDetailVersionActionComponent);
    component = fixture.componentInstance;
    component.productId = productId;
    router = TestBed.inject(Router);
    route = TestBed.inject(ActivatedRoute) as MockedObject<ActivatedRoute>;
    fixture.detectChanges();
    httpMock = TestBed.inject(HttpTestingController);
    environment.apiUrl = _originalApiUrl;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('first artifact should be chosen when select corresponding version', () => {
    const selectedVersion = 'Version 10.0.2';
    component.onSelectVersion(selectedVersion);
    expect(component.artifacts().length).toBe(0);
    const artifact = {
      name: 'Example Artifact',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true,
      id: { artifactId: 'example-artifactId' } as MavenArtifactKey
    } as ItemDropdown;
    component.versions.set([selectedVersion]);
    component.versionMap.set(selectedVersion, [artifact]);
    component.selectedVersion.set(selectedVersion);
    component.onSelectVersion(selectedVersion);

    expect(component.artifacts().length).toBe(1);
    expect(component.selectedArtifact).toEqual('https://example.com/download');
    expect(component.selectedArtifactId).toBe(artifact.id!.artifactId);
  });

  it('should update selectedVersion, artifacts, selectedArtifactName, and selectedArtifact, and call addVersionParamToRoute', () => {
    const version = '1.0';
    const artifacts = [
      {
        name: 'Example Artifact',
        downloadUrl: 'https://example.com/download',
        isProductArtifact: true
      } as ItemDropdown
    ];
    const versionMap = new Map<string, any[]>();
    versionMap.set(version, artifacts);

    // Set up spies
    vi.spyOn(component.selectedVersion, 'set');
    vi.spyOn(component as any, 'addVersionParamToRoute');

    // Mock data
    component.versionMap = versionMap;
    component.artifacts.set([]);

    // Call the method
    component.onSelectVersion(version);

    // Expectations
    expect(component.selectedVersion.set).toHaveBeenCalledWith(version);
    expect(component.artifacts()).toEqual(artifacts);
    expect(component.selectedArtifactName).toBe('Example Artifact');
    expect(component.selectedArtifact).toBe('https://example.com/download');
    expect(component.addVersionParamToRoute).toHaveBeenCalledWith(version);
  });

  it('should not update selected version or artifacts if the version is the same', () => {
    // Arrange
    const version = 'v1';
    component.selectedVersion.set(version);
    vi.spyOn(component.selectedVersion, 'set');
    vi.spyOn(component.artifacts, 'set');
    vi.spyOn(component.versionMap, 'get');
    vi.spyOn(component, 'addVersionParamToRoute');

    // Act
    component.onSelectVersion(version);

    // Assert
    expect(component.selectedVersion.set).not.toHaveBeenCalled();
    expect(component.artifacts.set).toHaveBeenCalled();
    expect(component.addVersionParamToRoute).toHaveBeenCalledWith(version);
  });

  it('should handle empty artifacts', () => {
    // Arrange
    const version = 'v1';
    vi.spyOn(component.selectedVersion, 'set');
    vi.spyOn(component.artifacts, 'set');
    vi.spyOn(component.versionMap, 'get').mockReturnValue([]);
    vi.spyOn(component, 'addVersionParamToRoute');

    // Act
    component.onSelectVersion(version);

    // Assert
    expect(component.selectedVersion.set).toHaveBeenCalledWith(version);
    expect(component.artifacts.set).toHaveBeenCalledWith([]);
    expect(component.selectedArtifactName).toBe('');
    expect(component.selectedArtifact).toBe('');
    expect(component.addVersionParamToRoute).toHaveBeenCalledWith(version);
  });

  it('should navigate with the selected version in the query params', () => {
    const version = '1.0';

    // Set up spy for router.navigate
    vi.spyOn(router, 'navigate').mockReturnValue(Promise.resolve(true));

    // Call the method
    component.addVersionParamToRoute(version);

    // Expectations
    expect(router.navigate).toHaveBeenCalledWith([], {
      relativeTo: route,
      fragment: 'description',
      queryParams: { [ROUTER.VERSION]: version },
      queryParamsHandling: 'merge'
    });
  });

  it('all of state should be reset before call rest api', () => {
    const selectedVersion = 'Version 10.0.2';
    const artifact = {
      name: 'Example Artifact',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true
    } as ItemDropdown;
    component.selectedVersion.set(selectedVersion);
    component.selectedArtifact = artifact.downloadUrl;
    component.versions().push(selectedVersion);
    component.artifacts().push(artifact);

    expect(component.versions().length).toBe(1);
    expect(component.artifacts().length).toBe(1);
    expect(component.selectedVersion()).toBe(selectedVersion);
    expect(component.selectedArtifact).toBe('https://example.com/download');
    component.sanitizeDataBeforeFetching();
    expect(component.versions().length).toBe(0);
    expect(component.artifacts().length).toBe(0);
    expect(component.selectedVersion()).toEqual(selectedVersion);
    expect(component.selectedArtifact).toEqual('');
    expect(component.selectedArtifactName).toEqual('');
    expect(component.versionMap.size).toBe(0);
  });

  it('should call sendRequestToProductDetailVersionAPI and update versions and versionMap', () => {
    const { mockArtifact1, mockArtifact2 } = mockApiWithExpectedResponse();
    component.selectedVersion.set('Version 1.0');
    component.getVersionWithArtifact(true);
    expect(
      productServiceMock.sendRequestToProductDetailVersionAPI
    ).toHaveBeenCalledWith(
      component.productId,
      component.isDevVersionsDisplayed(),
      component.designerVersion
    );

    expect(component.versions()).toEqual(['Version 1.0', 'Version 2.0']);
    expect(component.versionMap.get('Version 1.0')).toEqual([mockArtifact1]);
    expect(component.versionMap.get('Version 2.0')).toEqual([mockArtifact2]);
    expect(component.selectedVersion()).toBe('Version 1.0');
  });

  it('should call getVersionWithArtifact and toggle isDropDownDisplayed', () => {
    expect(component.isDropDownDisplayed()).toBe(false);

    mockApiWithExpectedResponse();
    component.onShowVersionAndArtifact();
    expect(component.isDropDownDisplayed()).toBe(true);
  });

  it('should send Api to get DevVersion', () => {
    component.isDevVersionsDisplayed.set(false);
    vi.spyOn(component.isDevVersionsDisplayed, 'set');
    expect(component.isDevVersionsDisplayed()).toBe(false);
    mockApiWithExpectedResponse();
    const event = new Event('click');
    vi.spyOn(event, 'preventDefault');
    component.onShowDevVersion(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.isDevVersionsDisplayed()).toBe(true);
  });

  function mockApiWithExpectedResponse() {
    const mockArtifact1 = {
      name: 'Example Artifact1',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true,
      label: 'Example Artifact1'
    } as ItemDropdown;
    const mockArtifact2 = {
      name: 'Example Artifact2',
      downloadUrl: 'https://example.com/download',
      label: 'Example Artifact2',
      isProductArtifact: true
    } as ItemDropdown;
    const mockData = [
      {
        version: '1.0',
        artifactsByVersion: [mockArtifact1]
      },
      {
        version: '2.0',
        artifactsByVersion: [mockArtifact2]
      }
    ];

    productServiceMock.sendRequestToProductDetailVersionAPI.mockReturnValue(
      of(mockData)
    );
    return { mockArtifact1: mockArtifact1, mockArtifact2: mockArtifact2 };
  }

  it('should call productService and update versions', () => {
    const productId = '123';
    component.versions.set([]);
    const mockVersions = [{ version: '1.0' }, { version: '2.0' }];
    productServiceMock.sendRequestToGetProductVersionsForDesigner.mockReturnValue(
      of(mockVersions)
    );

    component.isDevVersionsDisplayed.set(false);
    component.getVersionInDesigner();

    expect(
      productServiceMock.sendRequestToGetProductVersionsForDesigner
    ).toHaveBeenCalledWith(productId, false, '');
    expect(component.versions()).toEqual(['Version 1.0', 'Version 2.0']);

    component.isDevVersionsDisplayed.set(true);
    component.getVersionInDesigner();
    expect(
      productServiceMock.sendRequestToGetProductVersionsForDesigner
    ).toHaveBeenCalledWith(productId, true, '');
  });

  it('should handle empty response from productService', () => {
    component.versions.set([]);
    productServiceMock.sendRequestToGetProductVersionsForDesigner.mockReturnValue(
      of([])
    );

    // Act
    component.getVersionInDesigner();

    // Assert
    expect(
      productServiceMock.sendRequestToGetProductVersionsForDesigner
    ).toHaveBeenCalledWith(productId, true, '');
    expect(component.versions()).toEqual([]);
  });

  it('should return the correct tracking environment based on the action type', () => {
    const testCases = [
      {
        actionType: ProductDetailActionType.STANDARD,
        expected: MATOMO_TRACKING_ENVIRONMENT.standard
      },
      {
        actionType: ProductDetailActionType.DESIGNER_ENV,
        expected: MATOMO_TRACKING_ENVIRONMENT.designerEnv
      },
      {
        actionType: ProductDetailActionType.CUSTOM_SOLUTION,
        expected: MATOMO_TRACKING_ENVIRONMENT.customSolution
      }
    ];

    testCases.forEach(({ actionType, expected }) => {
      component.actionType = actionType;
      const result = component.getTrackingEnvironmentBasedOnActionType();
      expect(result).toBe(expected);
    });
  });

  it('should return empty environment when action type is default', () => {
    const result = component.getTrackingEnvironmentBasedOnActionType();
    expect(result).toBe('');
  });

  it('should close the dropdown when clicking outside', () => {
    component.isDropDownDisplayed.set(true);
    component.actionType = ProductDetailActionType.STANDARD;
    fixture.detectChanges();

    // If querySelector returns null, the element wasn't rendered;
    // in that case test the handler logic directly via signal inspection
    const domEl = (component as any).elementRef.nativeElement.querySelector?.('#download-dropdown-menu');
    if (!domEl) {
      // element not found in DOM - call handleClickOutside and verify via mock
      vi.spyOn((component as any).elementRef.nativeElement, 'querySelector').mockReturnValue({ contains: () => false });
    }

    const event = new MouseEvent('click');
    component.handleClickOutside(event);

    expect(component.isDropDownDisplayed()).toBe(false);
  });

  it('should set selected artifact properties correctly when onSelectArtifact is called', () => {
    const mockArtifact1 = {
      name: 'Example Artifact1',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true,
      label: 'Example Artifact1',
      id: { artifactId: 'example-artifactId' } as MavenArtifactKey
    } as ItemDropdown;

    component.onSelectArtifact(mockArtifact1);

    expect(component.selectedArtifactName).toBe(mockArtifact1.name);
    expect(component.selectedArtifact).toBe(mockArtifact1.downloadUrl);
    expect(component.selectedArtifactId).toBe(mockArtifact1.id!.artifactId);
  });

  it('should call selectedVersion.set with the correct version', () => {
    const testVersion = '1.2.3';
    component.onSelectVersionInDesigner(testVersion);
    expect(component.selectedVersion()).toBe(testVersion);
  });

  it('should fetch and trigger download with correct parameters', () => {
    const url = 'https://example.com/file.pdf';
    const fileName = 'artifact.zip';
    const testBlob = new Blob(['test'], { type: 'application/zip' });
    const downloadSpy = vi.spyOn<typeof component, 'fetchAndDownloadArtifact'>(component, 'fetchAndDownloadArtifact');
    vi.spyOn(component, 'onUpdateInstallationCount').mockImplementation(() => {});
    vi.spyOn(component, 'triggerDownload').mockImplementation(() => {});

    component.fetchAndDownloadArtifact(url, fileName);
    const req = httpMock.expectOne(url);
    req.flush(testBlob);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(downloadSpy).toHaveBeenCalledTimes(1);
    expect(downloadSpy).toHaveBeenCalledWith(url, fileName);
    expect(component.triggerDownload).toHaveBeenCalledWith(testBlob, fileName);
    expect(component.onUpdateInstallationCount).toHaveBeenCalled();
    expect(component.isDownloading()).toBe(false);
    httpMock.verify();
  });

  it('should return the correct marketplace service URL', () => {
    environment.apiUrl = 'https://api.example.com';

    expect(component.getMarketplaceServiceUrl()).toBe(
      'https://api.example.com'
    );

    environment.apiUrl = '/marketplace';
    expect(component.getMarketplaceServiceUrl()).toBe(
      globalThis.location.origin + '/marketplace'
    );
  });

  it('should generate correct URL and call fetchAndDownloadArtifact for DOC file', () => {
    component.selectedArtifact = 'document.doc';
    component.productId = '123';
    environment.apiUrl = 'https://api.example.com';
    component.productId = 'portal';
    component.selectedArtifactId = 'document.doc';
    component.selectedVersion.set('1.2.3');
    vi.spyOn(component, 'fetchAndDownloadArtifact');

    component.downloadArtifact();

    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-marketplace-data/portal/document.doc/1.2.3`,
      'document.doc'
    );
  });

  it('should correctly handle artifact download scenarios', () => {
    environment.apiUrl = 'https://api.example.com';
    component.productId = 'ai-assistant';
    component.selectedArtifactId = 'document.doc';
    component.selectedVersion.set('1.2.3');
    vi.spyOn(component, 'fetchAndDownloadArtifact');

    component.selectedArtifact =
      'https://example.com/ai-assistant-12.0.1.1.iar';

    component.downloadArtifact();
    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-marketplace-data/ai-assistant/document.doc/1.2.3`,
      'ai-assistant-12.0.1.1.iar'
    );

    component.isCheckedAppForEngine = true;
    component.selectedArtifactId = 'ai-assistant';
    component.selectedVersion.set('12.0.0');
    component.downloadArtifact();
    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-details/ai-assistant/ai-assistant/12.0.0/zip-file`,
      'ai-assistant-app-12.0.0.zip'
    );
  });

  it('should create a blob URL, create anchor, trigger click, and revoke URL', () => {
    const blob = new Blob(['test'], { type: 'text/plain' });
    const fileName = 'file.txt';
    const mockUrl = 'blob:http://localhost/fake-url';
    const createObjectURLSpy = vi
      .spyOn(URL, 'createObjectURL')
      .mockReturnValue(mockUrl);
    const revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL');
    const clickSpy = vi.fn();
    const anchorMock = {
      href: '',
      download: '',
      click: clickSpy
    } as unknown as HTMLAnchorElement;
    const createElementSpy = vi
      .spyOn(document, 'createElement')
      .mockReturnValue(anchorMock);
    component.triggerDownload(blob, fileName);
    expect(createObjectURLSpy).toHaveBeenCalledWith(blob);
    expect(createElementSpy).toHaveBeenCalledWith('a');
    expect(anchorMock.href).toBe(mockUrl);
    expect(anchorMock.download).toBe(fileName);
    expect(clickSpy).toHaveBeenCalled();
    expect(revokeObjectURLSpy).toHaveBeenCalledWith(mockUrl);
  });

  it('first artifact should be chosen when select corresponding version', () => {
    const selectedVersion = 'Version 10.0.2';
    component.onSelectVersion(selectedVersion);
    expect(component.artifacts().length).toBe(0);
    const artifact = {
      isProductArtifact: true,
      id: { artifactId: 'example-artifactId-1' } as MavenArtifactKey
    } as ItemDropdown;
    component.versions.set([selectedVersion]);
    component.versionMap.set(selectedVersion, [artifact]);
    component.selectedVersion.set(selectedVersion);
    component.onSelectVersion(selectedVersion);

    expect(component.artifacts().length).toBe(1);
    expect(component.selectedArtifactName).toEqual('');
    expect(component.selectedArtifact).toEqual('');
  });

  it('should update selectedArtifact values when existingArtifact is found', () => {
    const version = '1.0.0';
    const mockArtifacts = [
      {
        id: { artifactId: '123' },
        name: 'artifact-A',
        downloadUrl: 'http://example.com/a'
      } as ItemDropdown,
      {
        id: { artifactId: '456' },
        name: 'artifact-B',
        downloadUrl: 'http://example.com/b'
      } as ItemDropdown
    ];

    component.artifacts.set(mockArtifacts);
    component.selectedArtifactName = 'artifact-B';

    vi.spyOn(component, 'addVersionParamToRoute');
    component['updateSelectedArtifact'](version);

    expect(component.selectedArtifactId).toBe('456');
    expect(component.selectedArtifactName).toBe('artifact-B');
    expect(component.selectedArtifact).toBe('http://example.com/b');
    expect(component.addVersionParamToRoute).toHaveBeenCalledWith(version);
  });
});
