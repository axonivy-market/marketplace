import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnDestroy,
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
  MARKET_BASE_URL,
  NAME_COLUMN,
  REPORT_MODE,
  STANDARD_TAB
} from '../../../shared/constants/common.constant';
import { MonitoringCriteria } from '../../../shared/models/criteria.model';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { PAGE } from '../../../shared/constants/query.params.constant';
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
const SEARCH_DEBOUNCE_TIME = 500;

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
    LoadingSpinnerComponent
],
  templateUrl: './monitor-repo.component.html',
  styleUrl: './monitor-repo.component.scss'
})
export class MonitoringRepoComponent implements OnInit, OnDestroy {
  readonly COLUMN_NAME = NAME_COLUMN;
  readonly COLUMN_CI = CI_BUILD;
  readonly COLUMN_DEV = DEV_BUILD;
  readonly COLUMN_E2E = E2E_BUILD;

  @Input() tabKey!: string;
  @Input() activeTab = '';
  @Input() initialSearch = '';
  searchTextChanged = new Subject<string>();
  @Output() searchChange = new EventEmitter<string>();
  subscriptions: Subscription[] = [];
  mode: Record<string, RepoMode> = {};
  workflowKeys = [CI_BUILD, DEV_BUILD, E2E_BUILD];
  page = 1;
  pageSize = 10;
  totalElements = 0;
  protected LoadingComponentId = LoadingComponentId;
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
  router = inject(Router);
  route = inject(ActivatedRoute);
  PAGE = PAGE;

  ngOnInit() {
    if (!this.mode[this.tabKey]) {
      this.mode[this.tabKey] = DEFAULT_MODE;
    }
    let isFocusedProduct;
    if (this.activeTab === STANDARD_TAB) {
      isFocusedProduct = 'false';
    } else {
      isFocusedProduct = 'true';
    }
    this.criteria.isFocused = isFocusedProduct;
    this.criteria.search = this.initialSearch;
    this.subscriptions.push(
      this.searchTextChanged
        .pipe(debounceTime(SEARCH_DEBOUNCE_TIME))
        .subscribe(value => {
          this.criteria = {
            ...this.criteria,
            search: value
          };
          this.loadRepositories();

          let queryParams: { repoSearch: string | null } = { repoSearch: null };
          if (value) {
            queryParams = { repoSearch: this.criteria.search };
          }

          this.router.navigate([], {
            relativeTo: this.route,
            queryParamsHandling: 'merge',
            queryParams
          });
        })
    );
    this.loadRepositories();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['activeTab'] && !changes['activeTab'].firstChange) {
      this.resetDefaultPage();
      this.updateCriteriaAndLoad();
    }
  }

  resetDefaultPage() {
    this.page = 1;
    this.criteria.pageable.page = 0;
  }

  updateCriteriaAndLoad() {
    let isFocusedProduct;
    if (this.activeTab === STANDARD_TAB) {
      isFocusedProduct = 'false';
    } else {
      isFocusedProduct = 'true';
    }
    this.criteria.isFocused = isFocusedProduct;
    this.criteria.pageable.size = this.pageSize;
    this.criteria.pageable.page = this.page - 1;
    this.loadRepositories();
  }

  onSearchChanged(searchString: string) {
    this.page = 1;
    this.criteria.pageable.page = 0;
    this.criteria.pageable.size = this.pageSize;
    this.searchTextChanged.next(searchString);
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.criteria.pageable.page = newPage - 1;
    this.criteria.pageable.size = this.pageSize;
    this.loadRepositories();
  }

  onPageSizeChanged(newSize: number) {
    this.pageSize = newSize;
    this.page = 1;
    this.criteria.pageable.page = 0;
    this.criteria.pageable.size = this.pageSize;
    this.loadRepositories();
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
    this.loadRepositories();
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

  loadRepositories(): void {
    this.subscriptions.push(
      this.githubService.getRepositories(this.criteria).subscribe({
        next: data => {
          this.displayedRepositories = data?._embedded?.githubRepos || [];
          this.totalElements = data.page?.totalElements ?? 0;
        }
      })
    );
  }

  ngOnDestroy(): void {
    for (const sub of this.subscriptions) {
      sub.unsubscribe();
    }
  }

  protected readonly ALL_ITEMS_PAGE_SIZE = ALL_ITEMS_PAGE_SIZE;
}
