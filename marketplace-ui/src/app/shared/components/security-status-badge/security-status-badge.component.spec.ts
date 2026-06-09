import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityStatusBadgeComponent } from './security-status-badge.component';
import { TranslateModule } from '@ngx-translate/core';

describe('SecurityStatusBadgeComponent', () => {
  let component: SecurityStatusBadgeComponent;
  let fixture: ComponentFixture<SecurityStatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityStatusBadgeComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(SecurityStatusBadgeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('enabled input', () => {
    it('should render active badge when enabled is true', () => {
      component.enabled = true;
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.active');
      expect(badge).toBeTruthy();
    });

    it('should render inactive badge when enabled is false', () => {
      component.enabled = false;
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.none');
      expect(badge).toBeTruthy();
    });
  });

  describe('status input', () => {
    it('should render disabled badge when status is DISABLED', () => {
      component.status = 'DISABLED';
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.none');
      expect(badge).toBeTruthy();
    });

    it('should render no-permission badge when status is NO_PERMISSION', () => {
      component.status = 'NO_PERMISSION';
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.no-permission');
      expect(badge).toBeTruthy();
    });

    it('should render not-supported badge when status is NOT_SUPPORTED', () => {
      component.status = 'NOT_SUPPORTED';
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.none');
      expect(badge).toBeTruthy();
    });

    it('should render active badge when status is ENABLED and no alerts', () => {
      component.status = 'ENABLED';
      component.alerts = {};
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.active');
      expect(badge).toBeTruthy();
    });
  });

  describe('alerts input', () => {
    it('should render alert badges when alerts are present', () => {
      component.status = 'ENABLED';
      component.alerts = { critical: 2, high: 1 };
      fixture.detectChanges();

      const badges = fixture.nativeElement.querySelectorAll('.badge');
      expect(badges.length).toBe(2);
    });

    it('should render no vulnerabilities badge when alerts are empty', () => {
      component.status = 'ENABLED';
      component.alerts = { critical: 0 };
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.active');
      expect(badge).toBeTruthy();
    });
  });

  describe('alertCount input', () => {
    it('should render critical badge when alertCount is set', () => {
      component.status = 'ENABLED';
      component.alertCount = 3;
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.critical');
      expect(badge).toBeTruthy();
    });

    it('should render no vulnerabilities badge when alertCount is 0', () => {
      component.status = 'ENABLED';
      component.alertCount = 0;
      fixture.detectChanges();

      const badge = fixture.nativeElement.querySelector('.badge.active');
      expect(badge).toBeTruthy();
    });
  });

  describe('hasAlerts', () => {
    it('should return true when any alert value is greater than 0', () => {
      expect(component.hasAlerts({ critical: 1, high: 0 })).toBe(true);
    });

    it('should return false when all alert values are 0', () => {
      expect(component.hasAlerts({ critical: 0, high: 0 })).toBe(false);
    });
  });

  describe('alertKeys', () => {
    it('should return only keys with values greater than 0', () => {
      expect(component.alertKeys({ critical: 2, high: 0, medium: 1 })).toEqual(['critical', 'medium']);
    });

    it('should return empty array when all values are 0', () => {
      expect(component.alertKeys({ critical: 0 })).toEqual([]);
    });
  });
});