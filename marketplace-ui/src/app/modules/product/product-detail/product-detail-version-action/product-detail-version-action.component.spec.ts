import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductDetailVersionActionComponent } from './product-detail-version-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import { HttpParams, provideHttpClient } from '@angular/common/http';
import { ElementRef } from '@angular/core';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';
import { CookieService } from 'ngx-cookie-service';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { CommonUtils } from '../../../../shared/utils/common.utils';
import { ROUTER } from '../../../../shared/constants/router.constant';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { ProductDetailActionType } from '../../../../shared/enums/product-detail-action-type';
import { MATOMO_TRACKING_ENVIRONMENT } from '../../../../shared/constants/matomo.constant';
import { environment } from '../../../../../environments/environment';
import { API_URI } from '../../../../shared/constants/api.constant';

class MockElementRef implements ElementRef {
  nativeElement = {
    contains: jasmine.createSpy('contains')
  };
}

describe('ProductDetailVersionActionComponent', () => {
  const productId = '123';
  let component: ProductDetailVersionActionComponent;
  let fixture: ComponentFixture<ProductDetailVersionActionComponent>;
  let productServiceMock: any;
  let router: Router;
  let route: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(() => {
    productServiceMock = jasmine.createSpyObj('ProductService', [
      'sendRequestToProductDetailVersionAPI',
      'sendRequestToUpdateInstallationCount',
      'sendRequestToGetProductVersionsForDesigner'
    ]);
    const commonUtilsSpy = jasmine.createSpyObj('CommonUtils', [ 'getCookieValue' ]);
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
        provideHttpClient(),
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
      isProductArtifact: true
    } as ItemDropdown;
    component.versions.set([selectedVersion]);
    component.versionMap.set(selectedVersion, [artifact]);
    component.selectedVersion.set(selectedVersion);
    component.onSelectVersion(selectedVersion);

    expect(component.artifacts().length).toBe(1);
    expect(component.selectedArtifact).toEqual('https://example.com/download');
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

  it('should open the artifact download URL in a new window', () => {
    spyOn(window, 'open');
    component.selectedArtifact = 'https://example.com/download';
    spyOn(component, 'onUpdateInstallationCount');
    component.downloadArtifact();
    expect(window.open).toHaveBeenCalledWith(
      'https://example.com/download',
      '_blank'
    );
    expect(component.onUpdateInstallationCount).toHaveBeenCalledOnceWith();
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

  it('should open a new tab with the selected artifact URL', () => {
    const mockWindowOpen = jasmine.createSpy('windowOpen').and.returnValue({});
    spyOn(window, 'open').and.callFake(mockWindowOpen);
    spyOn(component, 'onUpdateInstallationCount');
    component.selectedArtifact = 'http://example.com/artifact';
    component.downloadArtifact();
    expect(window.open).toHaveBeenCalledWith(
      'http://example.com/artifact',
      '_blank'
    );
    expect(component.onUpdateInstallationCount).toHaveBeenCalledOnceWith();
  });

  it('should not call productService if versions are already populated', () => {
    component.versions.set(['1.0', '1.1']);
    fixture.detectChanges();
    component.getVersionInDesigner();
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).not.toHaveBeenCalled();
  });

  it('should call productService and update versions if versions are empty', () => {
    const productId = '123';
    component.versions.set([]);
    const mockVersions = [{ version: '1.0' }, { version: '2.0' }];
    productServiceMock.sendRequestToGetProductVersionsForDesigner.and.returnValue(of(mockVersions));

    // Act
    component.getVersionInDesigner();

    // Assert
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).toHaveBeenCalledWith(productId);
    expect(component.versions()).toEqual(['Version 1.0', 'Version 2.0']);
  });

  it('should handle empty response from productService', () => {
    component.versions.set([]);
    productServiceMock.sendRequestToGetProductVersionsForDesigner.and.returnValue(of([]));

    // Act
    component.getVersionInDesigner();

    // Assert
    expect(productServiceMock.sendRequestToGetProductVersionsForDesigner).toHaveBeenCalledWith(productId);
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
      label: 'Example Artifact1'
    } as ItemDropdown;
    component.onSelectArtifact(mockArtifact1);
    expect(component.selectedArtifactName).toBe(mockArtifact1.name);
    expect(component.selectedArtifact).toBe(mockArtifact1.downloadUrl);
    expect(component.selectedArtifactId).toBe(mockArtifact1.artifactId);
  });

  it('should call selectedVersion.set with the correct version', () => {
    const testVersion = '1.2.3';
    component.onSelectVersionInDesigner(testVersion);
    expect(component.selectedVersion()).toBe(testVersion);
  });

  it('should call onUpdateInstallationCount and open artifact URL when conditions are met (ZIP file)', () => {
    component.onUpdateInstallationCount = jasmine.createSpy(
      'onUpdateInstallationCount'
    );
    spyOn(window, 'open');
    component.downloadArtifact();
    expect(component.onUpdateInstallationCount).toHaveBeenCalled();
    expect(window.open).toHaveBeenCalledWith(
      component.selectedArtifact,
      '_blank'
    );
  });

  it('should open constructed URL when isCheckedAppForEngine is true', () => {
    component.isCheckedAppForEngine = true;
    component.onUpdateInstallationCount = jasmine.createSpy(
      'onUpdateInstallationCount'
    );
    spyOn(window, 'open');
    const expectedVersion = '1.2.3';
    component.selectedVersion.set(expectedVersion);
    const HTTP = 'http';
    let marketplaceServiceUrl = environment.apiUrl;
    if (!marketplaceServiceUrl.startsWith(HTTP)) {
      marketplaceServiceUrl = window.location.origin.concat(
        marketplaceServiceUrl
      );
    }
    const expectedParams = new HttpParams()
      .set(ROUTER.VERSION, expectedVersion)
      .set(ROUTER.ARTIFACT, component.selectedArtifactId ?? '')
      .toString();
    const expectedUrl = `${marketplaceServiceUrl}/${API_URI.PRODUCT_DETAILS}/${component.productId}/artifact/zip-file?${expectedParams}`;

    component.downloadArtifact();

    expect(window.open).toHaveBeenCalledWith(expectedUrl, '_blank');
  });
});
