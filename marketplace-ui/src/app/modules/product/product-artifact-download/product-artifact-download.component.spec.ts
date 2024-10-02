import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductArtifactDownloadComponent } from './product-artifact-download.component';

describe('ProductArtifactDownloadComponent', () => {
  let component: ProductArtifactDownloadComponent;
  let fixture: ComponentFixture<ProductArtifactDownloadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductArtifactDownloadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductArtifactDownloadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
