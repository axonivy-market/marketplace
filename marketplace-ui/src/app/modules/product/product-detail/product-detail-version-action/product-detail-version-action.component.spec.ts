import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductDetailVersionActionComponent } from './product-detail-version-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import { provideHttpClient } from '@angular/common/http';
import { Artifact } from '../../../../shared/models/vesion-artifact.model';
import { ElementRef } from '@angular/core';
import {provideHttpClientTesting} from "@angular/common/http/testing";

class MockElementRef implements ElementRef {
  nativeElement = {
    contains: jasmine.createSpy('contains')
  };
}
describe('ProductVersionActionComponent', () => {
  let component: ProductDetailVersionActionComponent;
  let fixture: ComponentFixture<ProductDetailVersionActionComponent>;
  let productServiceMock: any;
  let elementRef: MockElementRef;

  beforeEach(() => {
    productServiceMock = jasmine.createSpyObj('ProductService', [
      'sendRequestToProductDetailVersionAPI' , 'sendRequestToUpdateInstallationCount'
    ]);

    TestBed.configureTestingModule({
      imports: [ProductDetailVersionActionComponent, TranslateModule.forRoot()],
      providers: [
        TranslateService,
        provideHttpClient(),
        { provide: ProductService, useValue: productServiceMock },
        { provide: ElementRef, useClass: MockElementRef }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductDetailVersionActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('first artifact should be chosen when select corresponding version', () => {
    component.onSelectVersion();
    expect(component.artifacts().length).toBe(0);

    const selectedVersion = 'Version 10.0.2';
    const artifact = {
      name: 'Example Artifact',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true
    } as Artifact;
    component.versions.set([selectedVersion]);
    component.versionMap.set(selectedVersion, [artifact]);
    component.selectedVersion.set(selectedVersion);
    component.onSelectVersion();

    expect(component.artifacts().length).toBe(1);
    expect(component.selectedArtifact).toEqual('https://example.com/download');
  });

  it('all of state should be reset before call rest api', () => {
    const selectedVersion = 'Version 10.0.2';
    const artifact = {
      name: 'Example Artifact',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true
    } as Artifact;
    component.selectedVersion.set(selectedVersion);
    component.selectedArtifact = artifact.downloadUrl;
    component.versions().push(selectedVersion);
    component.artifacts().push(artifact);

    expect(component.versions().length).toBe(1);
    expect(component.artifacts().length).toBe(1);
    expect(component.selectedVersion()).toBe(selectedVersion);
    expect(component.selectedArtifact).toBe('https://example.com/download');
    component.sanitizeDataBeforFetching();
    expect(component.versions().length).toBe(0);
    expect(component.artifacts().length).toBe(0);
    expect(component.selectedVersion()).toEqual('');
    expect(component.selectedArtifact).toEqual('');
  });

  it('should call sendRequestToProductDetailVersionAPI and update versions and versionMap', () => {
    const { mockArtifct1, mockArtifct2 } = mockApiWithExpectedResponse();

    component.getVersionWithArtifact();

    expect(
      productServiceMock.sendRequestToProductDetailVersionAPI
    ).toHaveBeenCalledWith(
      component.productId,
      component.isDevVersionsDisplayed(),
      component.designerVersion
    );

    expect(component.versions()).toEqual(['Version 1.0', 'Version 2.0']);
    expect(component.versionMap.get('Version 1.0')).toEqual([mockArtifct1]);
    expect(component.versionMap.get('Version 2.0')).toEqual([mockArtifct2]);
    expect(component.selectedVersion()).toBe('Version 1.0');
  });

  it('should open the artifact download URL in a new window', () => {
    spyOn(window, 'open');
    component.selectedArtifact = 'https://example.com/download';
    spyOn(component, 'onUpdateInstallationCount');

    component.downloadArifact();

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
    expect(component.isDevVersionsDisplayed()).toBeFalse();
    mockApiWithExpectedResponse();
    const event = new Event('click');
    spyOn(event, 'preventDefault');
    component.onShowDevVersion(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.isDevVersionsDisplayed()).toBeTrue();
  });

  function mockApiWithExpectedResponse() {
    const mockArtifct1 = {
      name: 'Example Artifact1',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true
    } as Artifact;
    const mockArtifct2 = {
      name: 'Example Artifact2',
      downloadUrl: 'https://example.com/download',
      isProductArtifact: true
    } as Artifact;
    const mockData = [
      {
        version: '1.0',
        artifactsByVersion: [mockArtifct1]
      },
      {
        version: '2.0',
        artifactsByVersion: [mockArtifct2]
      }
    ];

    productServiceMock.sendRequestToProductDetailVersionAPI.and.returnValue(
      of(mockData)
    );
    return { mockArtifct1, mockArtifct2 };
  }

  it('should toggle isVersionsDropDownShow on calling onShowVersions', () => {
    const initialState = component.isVersionsDropDownShow();

    component.onShowVersions();
    expect(component.isVersionsDropDownShow()).toBe(!initialState);

    component.onShowVersions();
    expect(component.isVersionsDropDownShow()).toBe(initialState);
  });

  it('should not call onShowVersions if dropdown is not shown', () => {
    spyOn(component, 'isVersionsDropDownShow').and.returnValue(false);
    spyOn(component, 'onShowVersions');
    elementRef = TestBed.inject(ElementRef) as unknown as MockElementRef;

    const outsideEvent = new MouseEvent('click', {
      bubbles: true,
      cancelable: true,
      view: window
    });

    elementRef.nativeElement.contains.and.returnValue(false);

    document.dispatchEvent(outsideEvent);

    expect(component.onShowVersions).not.toHaveBeenCalled();
  });

  it('should open a new tab with the selected artifact URL', () => {
    const mockWindowOpen = jasmine.createSpy('windowOpen').and.returnValue({
      blur: jasmine.createSpy('blur')
    });

    const mockWindowFocus = spyOn(window, 'focus');

    // Mock window.open
    spyOn(window, 'open').and.callFake(mockWindowOpen);
    spyOn(component, 'onUpdateInstallationCount');
    // Set the artifact URL
    component.selectedArtifact = 'http://example.com/artifact';

    // Call the method
    component.downloadArifact();

    // Check if window.open was called with the correct URL and target
    expect(window.open).toHaveBeenCalledWith(
      'http://example.com/artifact',
      '_blank'
    );

    // Check if newTab.blur() was called
    expect(mockWindowOpen().blur).toHaveBeenCalled();
    expect(component.onUpdateInstallationCount).toHaveBeenCalledOnceWith();
    // Check if window.focus() was called
    expect(mockWindowFocus).toHaveBeenCalled();
  });
});
