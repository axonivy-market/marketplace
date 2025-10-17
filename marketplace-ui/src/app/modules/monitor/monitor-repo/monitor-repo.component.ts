import { Component, EventEmitter, inject, Input, OnChanges, OnInit, Output } from '@angular/core';
import { Repository } from '../github.service';
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
  ASCENDING,
  CI_BUILD,
  DEFAULT_MODE,
  DESCENDING,
  DEV_BUILD,
  E2E_BUILD,
  MARKET_BASE_URL,
  NAME_COLUMN,
  REPORT_MODE
} from '../../../shared/constants/common.constant';

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
export class MonitoringRepoComponent implements OnInit, OnChanges {
  readonly COLUMN_NAME = NAME_COLUMN;
  readonly COLUMN_CI = CI_BUILD;
  readonly COLUMN_DEV = DEV_BUILD;
  readonly COLUMN_E2E = E2E_BUILD;

  @Input() repositories: Repository[] = [];
  @Input() isStandardTab = false;
  @Input() tabKey!: string;
  @Input() initialFilter = '';
  @Output() searchChange = new EventEmitter<string>();

  mode: Record<string, RepoMode> = {};
  workflowKeys = [CI_BUILD, DEV_BUILD, E2E_BUILD];
  searchText = '';
  page = 1;
  pageSize = 10;
  sortColumn = this.COLUMN_NAME;
  sortDirection = ASCENDING;
  allRepositories: Repository[] = [];
  filteredRepositories: Repository[] = [];
  displayedRepositories: Repository[] = [];

  languageService = inject(LanguageService);
  translateService = inject(TranslateService);

  ngOnInit() {
    if (!this.mode[this.tabKey]) {
      this.mode[this.tabKey] = DEFAULT_MODE;
    }

    if (this.initialFilter) {
      this.searchText = this.initialFilter;
    }
  }

  ngOnChanges() {
    this.allRepositories = [...this.repositories];
    const filterText = this.searchText || this.initialFilter;
    this.applyFilter(filterText);
  }

  onSearchChanged(searchString: string) {
    this.searchText = searchString;
    this.applyFilter(searchString);
  }

  applyFilter(search: string) {
    if (search && search.trim().length > 0) {
      this.filteredRepositories = this.allRepositories.filter(repo =>
        repo.repoName.toLowerCase().includes(search.toLowerCase())
      );
    } else {
      this.filteredRepositories = [...this.allRepositories];
    }

    this.refreshPagination();
  }

  getPageSize(): number {
    if (this.pageSize === -1) {
      return this.filteredRepositories.length || 1;
    } else {
      return this.pageSize;
    }
  }

  getCollectionSize(): number {
    return this.filteredRepositories.length;
  }

  refreshPagination() {
    if (this.pageSize === -1) {
      this.displayedRepositories = [...this.filteredRepositories];
    } else {
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.displayedRepositories = this.filteredRepositories.slice(start, end);
    }
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

    this.filteredRepositories.sort((repo1, repo2) => {
      const repo1ColumnValue =
        this.getColumnValue(repo1, column)?.toString().toLowerCase() ?? '';
      const repo2ColumnValue =
        this.getColumnValue(repo2, column)?.toString().toLowerCase() ?? '';

      return this.compareColumnValues(repo1ColumnValue, repo2ColumnValue, this.sortDirection);

    });

    this.refreshPagination();
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
}
