import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NavigationComponent } from './navigation.component';
import { RouterModule } from '@angular/router';

describe('NavigationComponent', () => {
  let component: NavigationComponent;
  let fixture: ComponentFixture<NavigationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NavigationComponent,
        TranslateModule.forRoot(),
        RouterModule.forRoot([])
      ],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(NavigationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return navItems when isAdminPage is false', () => {
    component.isAdminPage = false;
    expect(component.items).toBe(component.navItems);
  });

  it('should return adminNavItems when isAdminPage is true', () => {
    component.isAdminPage = true;
    expect(component.items).toBe(component.adminNavItems);
  });
});
