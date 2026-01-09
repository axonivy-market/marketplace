import { ComponentFixture, TestBed } from '@angular/core/testing';
import { QuickAccessComponent } from './quick-access.component';
import { TranslateModule } from '@ngx-translate/core';
import { PageTitleService } from '../../../shared/services/page-title.service';

describe('QuickAccessComponent', () => {
  let component: QuickAccessComponent;
  let fixture: ComponentFixture<QuickAccessComponent>;
  let pageTitleService: jasmine.SpyObj<PageTitleService>;

  beforeEach(async () => {
    pageTitleService = jasmine.createSpyObj('PageTitleService', [
      'setTitleOnLangChange'
    ]);

    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: PageTitleService, useValue: pageTitleService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(QuickAccessComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call setTitleOnLangChange on init', () => {
    fixture.detectChanges();

    expect(pageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.quickAccess.pageTitle'
    );
  });
});
