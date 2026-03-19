import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ProductDetailMavenContentComponent } from './product-detail-maven-content.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MOCK_PRODUCT_MODULE_CONTENT } from '../../../../shared/mocks/mock-data';

describe('ProductDetailMavenContentComponent', () => {
  let component: ProductDetailMavenContentComponent;
  let fixture: ComponentFixture<ProductDetailMavenContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailMavenContentComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailMavenContentComponent);
    component = fixture.componentInstance;
    component.productModuleContent = MOCK_PRODUCT_MODULE_CONTENT;
    component.productName = 'Test Product';
    component.selectedVersion = '10.0.10';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render maven values from productModuleContent', () => {
    const codeElement: HTMLElement = fixture.debugElement.query(By.css('code')).nativeElement;

    expect(codeElement.textContent).toContain(`${component.productModuleContent.groupId}`);
    expect(codeElement.textContent).toContain(`${component.productModuleContent.artifactId}`);
    expect(codeElement.textContent).toContain('10.0.10');
    expect(codeElement.textContent).toContain(`${component.productModuleContent.type}`);
  });

  it('should update displayed groupId and artifactId when productModuleContent changes', () => {
    component.productModuleContent = {
      ...component.productModuleContent,
      groupId: 'com.axonivy.connector.new.jira',
      artifactId: 'new-jira-connector'
    };
    component.selectedVersion = '11.0.0';

    fixture.detectChanges();
    const codeElement: HTMLElement = fixture.debugElement.query(By.css('code')).nativeElement;

    expect(codeElement.textContent).toContain('com.axonivy.connector.new.jira');
    expect(codeElement.textContent).toContain('new-jira-connector');
    expect(codeElement.textContent).toContain('11.0.0');
  });
});
