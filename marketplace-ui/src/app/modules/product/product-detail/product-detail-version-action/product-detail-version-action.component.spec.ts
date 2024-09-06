import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductDetailVersionActionComponent } from './product-detail-version-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import { provideHttpClient } from '@angular/common/http';
import { ElementRef } from '@angular/core';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';

class MockElementRef implements ElementRef {
  nativeElement = {
    contains: jasmine.createSpy('contains')
  };
}
describe('ProductDetailVersionActionComponent', () => {
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
    expect(component.selectedVersion()).toEqual('');
    expect(component.selectedArtifact).toEqual('');
  });

  it('should call sendRequestToProductDetailVersionAPI and update versions and versionMap', () => {
    const { mockArtifact1, mockArtifact2 } = mockApiWithExpectedResponse();

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
      isProductArtifact: true, label: 'Example Artifact1',
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
    const mockWindowOpen = jasmine.createSpy('windowOpen').and.returnValue({
      blur: jasmine.createSpy('blur')
    });
    const mockWindowFocus = spyOn(window, 'focus');
    spyOn(window, 'open').and.callFake(mockWindowOpen);
    spyOn(component, 'onUpdateInstallationCount');
    component.selectedArtifact = 'http://example.com/artifact';

    component.downloadArtifact();

    expect(window.open).toHaveBeenCalledWith(
      'http://example.com/artifact',
      '_blank'
    );
    expect(mockWindowOpen().blur).toHaveBeenCalled();
    expect(component.onUpdateInstallationCount).toHaveBeenCalledOnceWith();
    expect(mockWindowFocus).toHaveBeenCalled();
  });

  it('should open a new tab with the correct URL and blur it', () => {
    const productId = 'octopus';
    component.productId = productId;
    const newTabMock: Partial<Window> = {
    blur: jasmine.createSpy('blur')
    };
    spyOn(window, 'open').and.returnValue(newTabMock as Window);
    spyOn(window, 'focus');
    component.onNavigateToContactPage();

    expect(window.open).toHaveBeenCalledWith(
      `https://www.axonivy.com/marketplace/contact/?market_solutions=${productId}`,
      '_blank'
    );
    expect(newTabMock.blur).toHaveBeenCalled();
    expect(window.focus).toHaveBeenCalled();
  });

  it('should not call blur if newTab is null', () => {
    const productId = 'octopus';
    component.productId = productId;
    spyOn(window, 'open').and.returnValue(null);
    spyOn(window, 'focus');

    component.onNavigateToContactPage();

    expect(window.open).toHaveBeenCalledWith(
      `https://www.axonivy.com/marketplace/contact/?market_solutions=${productId}`,
      '_blank'
    );
    expect(window.focus).toHaveBeenCalled();
  });
});
