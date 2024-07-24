import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductInstallationCountActionComponent } from './product-installation-count-action.component';
import {MarkdownModule} from "ngx-markdown";
import {TranslateModule} from "@ngx-translate/core";

describe('ProductInstallationCountActionComponent', () => {
  let component: ProductInstallationCountActionComponent;
  let fixture: ComponentFixture<ProductInstallationCountActionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProductInstallationCountActionComponent,
        TranslateModule.forRoot(),
        MarkdownModule.forRoot()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductInstallationCountActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
