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
  NgbTypeaheadModule,
} from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ProductFilterComponent } from '../../product/product-filter/product-filter.component';
import { RepoTestResultComponent } from '../repo-test-result/repo-test-result.component';
import {
  ASCENDING,
  CI_BUILD,
  DEFAULT_MODE,
  DEFAULT_MONITORING_PAGEABLE,
  DEFAULT_PAGEABLE,
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
    RepoTestResultComponent,
  ],
  templateUrl: './monitor-repo.component.html',
  styleUrl: './monitor-repo.component.scss',
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
  searchText = '';
  page = 1;
  pageSize = 10;
  totalElements = 0;
  sortColumn = this.COLUMN_NAME;
  sortDirection = ASCENDING;
  displayedRepositories: Repository[] = [];
  criteria: MonitoringCriteria = {
    search: '',
    isFocused: 'true',
    pageable: DEFAULT_MONITORING_PAGEABLE,
  };
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  githubService = inject(GithubService);

  ngOnInit() {
    if (!this.mode[this.tabKey]) {
      this.mode[this.tabKey] = DEFAULT_MODE;
    }

    if (this.initialFilter) {
      this.searchText = this.initialFilter;
    }

    this.criteria.search = this.searchText;
    this.loadRepositories(this.criteria);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['activeTab'] && !changes['activeTab'].firstChange) {
      this.resetDefaultPage();
      this.updateCriteriaAndLoad();
    }
    if (changes['initialFilter'] && !changes['initialFilter'].firstChange) {
      this.searchText = this.initialFilter;
      this.resetDefaultPage();
      this.updateCriteriaAndLoad();
    }
  }

  resetDefaultPage() {
    this.page = 1;
    this.criteria.pageable.page = 0;
  }

  updateCriteriaAndLoad() {
    if (this.activeTab != STANDARD_TAB) {
      this.criteria.isFocused = 'true';
    } else {
      this.criteria.isFocused = '';
    }
    this.criteria.search = this.searchText;
    this.criteria.pageable.size = this.pageSize;
    this.criteria.pageable.page = this.page - 1;
    this.loadRepositories(this.criteria);
  }

  onSearchChanged(searchString: string) {
    this.searchText = searchString;
    this.page = 1; // reset về page đầu khi search
    this.criteria.pageable.page = 0;
    this.criteria.pageable.size = this.pageSize;
    this.criteria.search = this.searchText;
    this.loadRepositories(this.criteria);
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.criteria.pageable.page = newPage - 1;
    this.criteria.pageable.size = this.pageSize;
    this.criteria.search = this.searchText;
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
    console.log('sortRepositoriesByColumn', column);
    if (this.sortColumn === column) {
      this.toggleSortDirection();
    } else {
      this.sortColumn = column;
      this.sortDirection = ASCENDING;
    }

    this.displayedRepositories.sort((repo1, repo2) => {
      const repo1ColumnValue =
        this.getColumnValue(repo1, column)?.toString().toLowerCase() ?? '';
      const repo2ColumnValue =
        this.getColumnValue(repo2, column)?.toString().toLowerCase() ?? '';

      return this.compareColumnValues(repo1ColumnValue, repo2ColumnValue, this.sortDirection);
    });
  }

  private toggleSortDirection() {
    if (this.sortDirection === ASCENDING) {
      this.sortDirection = DESCENDING;
    } else {
      this.sortDirection = ASCENDING;
    }
  }

  private compareColumnValues(repo1ColumnValue: string, repo2ColumnValue: string, sortDirection: string): number {
    const columnValueComparison = repo1ColumnValue.localeCompare(repo2ColumnValue);
    if (columnValueComparison === 0) {
      return 0;
    }

    const isAscendingDirection = sortDirection === ASCENDING;
    if (isAscendingDirection) {
      return columnValueComparison;
    } else {
      return -columnValueComparison;
    }
  }

  private getColumnValue(repo: Repository, column: string): string {
    if (column === this.COLUMN_NAME) {
      return repo.repoName;
    }

    return this.findWorkflowMatch(repo, column)?.conclusion ?? '';
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
      next: (data) => {
        this.displayedRepositories = data._embedded.githubRepos;
        this.totalElements = data.page?.totalElements || 0;
      },
      error: (err) => {},
    });
  }
}
