import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductDetailVersionActionComponent } from './product-detail-version-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ElementRef } from '@angular/core';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';
import { CookieService } from 'ngx-cookie-service';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { CommonUtils } from '../../../../shared/utils/common.utils';
import { ROUTER } from '../../../../shared/constants/router.constant';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { ProductDetailActionType } from '../../../../shared/enums/product-detail-action-type';
import { MATOMO_TRACKING_ENVIRONMENT } from '../../../../shared/constants/matomo.constant';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../../environments/environment';
import { MavenArtifactKey } from '../../../../shared/models/maven-artifact.model';

class MockElementRef implements ElementRef {
  nativeElement = {
    contains: jasmine.createSpy('contains')
  };
}

describe('ProductDetailVersionActionComponent', () => {
  const productId = '123';
  let component: ProductDetailVersionActionComponent;
  let fixture: ComponentFixture<ProductDetailVersionActionComponent>;
  let  productServiceMock: any;
  let router: Router;
  let route: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(() => {
    productServiceMock = jasmine.createSpyObj('ProductService', [
      'sendRequestToProductDetailVersionAPI',
      'sendRequestToGetInstallationCount',
      'sendRequestToGetProductVersionsForDesigner'
    ]);
    const commonUtilsSpy = jasmine.createSpyObj('CommonUtils', ['getCookieValue']);
    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: {
        queryParams: {}
      }
    });

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
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
        { provide: CommonUtils, useValue: commonUtilsSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductDetailVersionActionComponent);
    component = fixture.componentInstance;
    component.productId = productId;
    router = TestBed.inject(Router);
    route = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });

  it('first artifact should be chosen when select corresponding version', () => {
    const selectedVersion = 'Version 10.0.2';
    component.onSelectVersion(selectedVersion);
    expect(component.artifacts().length).toBe(0);
    const artifact = {
      name: 'Example Artifact',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true,
      id: { artifactId: "example-artifactId" } as MavenArtifactKey
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
    spyOn(component.selectedVersion, 'set');
    spyOn(component as any, 'addVersionParamToRoute').and.callThrough();

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
    spyOn(component.selectedVersion, 'set');
    spyOn(component.artifacts, 'set');
    spyOn(component.versionMap, 'get');
    spyOn(component, 'addVersionParamToRoute');

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
    spyOn(component.selectedVersion, 'set');
    spyOn(component.artifacts, 'set');
    spyOn(component.versionMap, 'get').and.returnValue([]);
    spyOn(component, 'addVersionParamToRoute');

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
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    // Call the method
    component.addVersionParamToRoute(version);

    // Expectations
    expect(router.navigate).toHaveBeenCalledWith([], {
      relativeTo: route,
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
  });

  it('should call sendRequestToProductDetailVersionAPI and update versions and versionMap', () => {
    const { mockArtifact1, mockArtifact2 } = mockApiWithExpectedResponse();
    component.selectedVersion.set('Version 1.0');
    component.getVersionWithArtifact();
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
    expect(component.isDropDownDisplayed()).toBeFalse();

    mockApiWithExpectedResponse();
    component.onShowVersionAndArtifact();
    expect(component.isDropDownDisplayed()).toBeTrue();
  });

  it('should send Api to get DevVersion', () => {
    component.isDevVersionsDisplayed.set(false);
    spyOn(component.isDevVersionsDisplayed, 'set');
    expect(component.isDevVersionsDisplayed()).toBeFalse();
    mockApiWithExpectedResponse();
    const event = new Event('click');
    spyOn(event, 'preventDefault');
    component.onShowDevVersion(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.isDevVersionsDisplayed()).toBeTrue();
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

    productServiceMock.sendRequestToProductDetailVersionAPI.and.returnValue(
      of(mockData)
    );
    return { mockArtifact1: mockArtifact1, mockArtifact2: mockArtifact2 };
  }

  it('should call productService and update versions', () => {
    const productId = '123';
    component.versions.set([]);
    const mockVersions = [{ version: '1.0' }, { version: '2.0' }];
    productServiceMock.sendRequestToGetProductVersionsForDesigner.and.returnValue(of(mockVersions));

    // Act
    component.getVersionInDesigner();

    // Assert
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).toHaveBeenCalledWith(productId, false, '');
    expect(component.versions()).toEqual(['Version 1.0', 'Version 2.0']);

    component.isDevVersionsDisplayed.set(true);
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).toHaveBeenCalledWith(productId, true, '');
  });

  it('should handle empty response from productService', () => {
    component.versions.set([]);
    productServiceMock.sendRequestToGetProductVersionsForDesigner.and.returnValue(of([]));

    // Act
    component.getVersionInDesigner();

    // Assert
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).toHaveBeenCalledWith(productId, '');
    expect(component.versions()).toEqual([]);
  });

  it('should return the correct tracking environment based on the action type', () => {
    const testCases = [
      { actionType: ProductDetailActionType.STANDARD, expected: MATOMO_TRACKING_ENVIRONMENT.standard },
      { actionType: ProductDetailActionType.DESIGNER_ENV, expected: MATOMO_TRACKING_ENVIRONMENT.designerEnv },
      { actionType: ProductDetailActionType.CUSTOM_SOLUTION, expected: MATOMO_TRACKING_ENVIRONMENT.customSolution }
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

    const event = new MouseEvent('click');
    document.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.isDropDownDisplayed()).toBeFalse();
  });

  it('should set selected artifact properties correctly when onSelectArtifact is called', () => {
    const mockArtifact1 = {
      name: 'Example Artifact1',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true,
      label: 'Example Artifact1',
      id: { artifactId: "example-artifactId" } as MavenArtifactKey
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

  it('should properly handle file download', () => {
    spyOn(document.body, 'appendChild');
    spyOn(document.body, 'removeChild');

    const mockClick = jasmine.createSpy();
    spyOn(document, 'createElement').and.returnValue({ click: mockClick } as any);

    component['downloadFile']('base64Data', 'test.zip');

    expect(document.body.appendChild).toHaveBeenCalled();
    expect(mockClick).toHaveBeenCalled();
    expect(document.body.removeChild).toHaveBeenCalled();
  });

  it('should call sendRequestToGetInstallationCount and emit installation count', fakeAsync(() => {
    productServiceMock.sendRequestToGetInstallationCount.and.returnValue(of(42));
    spyOn(component.installationCount, 'emit');

    component.onUpdateInstallationCountForDesigner();
    tick(1000);

    expect(productServiceMock.sendRequestToGetInstallationCount).toHaveBeenCalledWith(component.productId);
    expect(component.installationCount.emit).toHaveBeenCalledWith(42);
  }));

  it('should return the correct marketplace service URL', () => {
    environment.apiUrl = 'https://api.example.com';

    expect(component.getMarketplaceServiceUrl()).toBe('https://api.example.com');

    environment.apiUrl = '/marketplace';
    expect(component.getMarketplaceServiceUrl()).toBe(window.location.origin + '/marketplace');
  });

  it('should generate correct URL and call fetchAndDownloadArtifact for DOC file', () => {
    component.selectedArtifact = 'document.doc';
    component.productId = '123';
    environment.apiUrl = 'https://api.example.com';

    spyOn(component, 'fetchAndDownloadArtifact');

    component.downloadArtifact();

    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-marketplace-data/version-download/123?url=document.doc`,
      'document.doc'
    );
  });

  it('should correctly handle artifact download scenarios', () => {
    environment.apiUrl = 'https://api.example.com';
    component.productId = 'ai-assistant';
    spyOn(component, 'fetchAndDownloadArtifact');

    component.selectedArtifact = 'https://example.com/ai-assistant-12.0.1.1.iar';

    component.downloadArtifact();
    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-marketplace-data/version-download/ai-assistant?url=https://example.com/ai-assistant-12.0.1.1.iar`, 'ai-assistant-12.0.1.1.iar'
    );

    component.isCheckedAppForEngine = true;
    component.selectedArtifactId = 'ai-assistant';
    component.selectedVersion.set('12.0.0');
    component.downloadArtifact();
    expect(component.fetchAndDownloadArtifact).toHaveBeenCalledWith(
      `${environment.apiUrl}/api/product-details/ai-assistant/artifact/zip-file?version=12.0.0&artifact=ai-assistant`,
      'ai-assistant-app-12.0.0.zip'
    );
  });
});
