import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule } from '@angular/common';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BuildBadgeTooltipComponent } from '../build-badge-tooltip/build-badge-tooltip.component';
import {
  NgbTooltipModule,
  NgbPagination,
  NgbPaginationModule,
  NgbTypeaheadModule
} from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ProductFilterComponent } from '../../product/product-filter/product-filter.component';
import { RepoTestResultComponent } from '../repo-test-result/repo-test-result.component';
import {
  ALL_ITEMS_PAGE_SIZE,
  ASCENDING,
  CI_BUILD,
  DEFAULT_MODE,
  DEFAULT_MONITORING_PAGEABLE,
  DESCENDING,
  DEV_BUILD,
  E2E_BUILD,
  FOCUSED_TAB,
  MARKET_BASE_URL,
  NAME_COLUMN,
  REPORT_MODE,
  STANDARD_TAB
} from '../../../shared/constants/common.constant';
import { MonitoringCriteria } from '../../../shared/models/criteria.model';

export type RepoMode = typeof DEFAULT_MODE | typeof REPORT_MODE;

@Component({
  selector: 'app-monitor-repo',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BuildBadgeTooltipComponent,
    NgbTooltipModule,
    FormsModule,
    NgbPagination,
    NgbTypeaheadModule,
    NgbPaginationModule,
    ProductFilterComponent,
    RepoTestResultComponent
  ],
  templateUrl: './monitor-repo.component.html',
  styleUrl: './monitor-repo.component.scss'
})
export class MonitoringRepoComponent implements OnInit {
  readonly COLUMN_NAME = NAME_COLUMN;
  readonly COLUMN_CI = CI_BUILD;
  readonly COLUMN_DEV = DEV_BUILD;
  readonly COLUMN_E2E = E2E_BUILD;

  @Input() tabKey!: string;
  @Input() initialFilter = '';
  @Input() activeTab = '';
  @Output() searchChange = new EventEmitter<string>();

  mode: Record<string, RepoMode> = {};
  workflowKeys = [CI_BUILD, DEV_BUILD, E2E_BUILD];
  page = 1;
  pageSize = 10;
  totalElements = 0;
  sortColumn = this.COLUMN_NAME;
  sortDirection = ASCENDING;
  displayedRepositories: Repository[] = [];
  criteria: MonitoringCriteria = {
    search: '',
    isFocused: 'true',
    sortDirection: ASCENDING,
    workflowType: 'name',
    pageable: DEFAULT_MONITORING_PAGEABLE
  };
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  githubService = inject(GithubService);

  ngOnInit() {
    if (!this.mode[this.tabKey]) {
      this.mode[this.tabKey] = DEFAULT_MODE;
    }

    if (this.initialFilter) {
      this.criteria.search = this.initialFilter;
    }

    this.loadRepositories(this.criteria);

    if (this.initialFilter) {
      this.activeTab = this.displayedRepositories[0]?.focused ? FOCUSED_TAB : STANDARD_TAB;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['activeTab'] && !changes['activeTab'].firstChange) {
      this.resetDefaultPage();
      this.updateCriteriaAndLoad();
    }
    if (changes['initialFilter'] && !changes['initialFilter'].firstChange) {
      this.criteria.search = this.initialFilter;
      this.resetDefaultPage();
      this.updateCriteriaAndLoad();
    }
  }

  resetDefaultPage() {
    this.page = 1;
    this.criteria.pageable.page = 0;
  }

  updateCriteriaAndLoad() {
    if (this.activeTab !== STANDARD_TAB) {
      this.criteria.isFocused = 'true';
    } else {
      this.criteria.isFocused = '';
    }
    this.criteria.pageable.size = this.pageSize;
    this.criteria.pageable.page = this.page - 1;
    this.loadRepositories(this.criteria);
  }

  onSearchChanged(searchString: string) {
    this.page = 1;
    this.criteria.pageable.page = 0;
    this.criteria.pageable.size = this.pageSize;
    this.criteria.search = searchString;
    this.loadRepositories(this.criteria);
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.criteria.pageable.page = newPage - 1;
    this.criteria.pageable.size = this.pageSize;
    this.loadRepositories(this.criteria);
  }

  onPageSizeChanged(newSize: number) {
    this.pageSize = newSize;
    this.page = 1;
    this.criteria.pageable.page = 0;
    this.criteria.pageable.size = this.pageSize;
    this.loadRepositories(this.criteria);
  }

  getMarketUrl(productId: string): string {
    return `${MARKET_BASE_URL}${encodeURIComponent(productId)}`;
  }

  sortRepositoriesByColumn(column: string) {
    if (this.sortColumn === column) {
      this.toggleSortDirection();
    } else {
      this.sortColumn = column;
      this.sortDirection = ASCENDING;
    }
    this.criteria.sortDirection = this.sortDirection;
    this.criteria.workflowType = this.sortColumn;
    this.loadRepositories(this.criteria);
  }

  private toggleSortDirection() {
    if (this.sortDirection === ASCENDING) {
      this.sortDirection = DESCENDING;
    } else {
      this.sortDirection = ASCENDING;
    }
  }

  findWorkflowMatch(repo: Repository, workflow: string) {
    return repo.workflowInformation?.find(wf => wf.workflowType === workflow);
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) {
      return '';
    }

    if (this.sortDirection === ASCENDING) {
      return 'bi bi-arrow-up';
    } else {
      return 'bi bi-arrow-down';
    }
  }

  loadRepositories(criteria: MonitoringCriteria): void {
    this.githubService.getRepositories(criteria).subscribe({
      next: data => {
        this.displayedRepositories = data?._embedded?.githubRepos || [];
        this.totalElements = data.page?.totalElements ?? 0;
      }
    });
  }

  protected readonly ALL_ITEMS_PAGE_SIZE = ALL_ITEMS_PAGE_SIZE;
}
