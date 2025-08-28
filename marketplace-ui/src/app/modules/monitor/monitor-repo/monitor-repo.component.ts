import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BuildBadgeTooltipComponent } from '../build-badge-tooltip/build-badge-tooltip.component';
import { NgbTooltipModule, NgbPagination, NgbPaginationModule, NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { FormsModule } from '@angular/forms';
import { ProductFilterComponent } from "../../product/product-filter/product-filter.component";
import { RepoTestResultComponent } from "../repo-test-result/repo-test-result.component";
import { ASCENDING, DEFAULT_MODE, DESCENDING, MARKET_BASE_URL, REPORT_MODE } from '../../../shared/constants/common.constant';

export type RepoMode = typeof DEFAULT_MODE | typeof REPORT_MODE;

@Component({
  selector: 'app-monitor-repo',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BuildBadgeTooltipComponent,
    NgbTooltipModule,
    LoadingSpinnerComponent,
    FormsModule,
    NgbPagination,
    NgbTypeaheadModule, NgbPaginationModule,
    ProductFilterComponent,
    RepoTestResultComponent
  ],
  templateUrl: './monitor-repo.component.html',
  styleUrl: './monitor-repo.component.scss'
})
export class MonitoringRepoComponent {
  protected LoadingComponentId = LoadingComponentId;
  @Input() repositories: Repository[] = [];
  @Input() isStandardTab = false;
  @Input() isLoading = false;
  @Input() tabKey!: string;
  @Output() searchChange = new EventEmitter<string>();

  mode: Record<string, RepoMode> = {};
  searchText = '';
  page = 1;
  pageSize = 10;
  sortColumn: string = '';
  sortDirection: typeof ASCENDING | typeof DESCENDING = ASCENDING;
  allRepositories: Repository[] = [];
  filteredRepositories: Repository[] = [];
  displayedRepositories: Repository[] = [];

  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  translateService = inject(TranslateService);
  router = inject(Router);
  pageTitleService: PageTitleService = inject(PageTitleService);

  ngOnInit() {
    if (!this.mode[this.tabKey]) {
      this.mode[this.tabKey] = DEFAULT_MODE;
    }
  }

  ngOnChanges() {
    this.allRepositories = [...this.repositories];
    this.applyFilter(this.searchText);
  }

  onSearchChanged(searchString: string) {
    this.searchText = searchString;
    this.page = 1;
    this.applyFilter(searchString);
  }

  applyFilter(search: string) {
    if (search && search.trim().length > 0) {
      this.filteredRepositories = this.allRepositories.filter(repo =>
        repo.name.toLowerCase().includes(search.toLowerCase())
      );
    } else {
      this.filteredRepositories = [...this.allRepositories];
    }

    this.refreshPagination();
  }

  refreshPagination() {
    const start = (this.page - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.displayedRepositories = this.filteredRepositories.slice(start, end);
  }

  getTestResultsForWorkflow(repo: Repository, workflow: string) {
    return repo.testResults.find(build => build.workflow === workflow);
  }

  getMarketUrl(repoName: string): string {
    return `${MARKET_BASE_URL}${encodeURIComponent(repoName)}`;
  }

  sortRepositoriesByColumn(column: keyof Repository) {
  if (this.sortColumn === column) {
    this.sortDirection = this.sortDirection === ASCENDING ? DESCENDING : ASCENDING;
  } else {
    this.sortColumn = column;
    this.sortDirection = ASCENDING;
  }

  this.filteredRepositories.sort((repo1, repo2) => {
    const firstValue = (repo1[column] ?? '').toString().toLowerCase();
    const secondValue = (repo2[column] ?? '').toString().toLowerCase();

    if (firstValue < secondValue) return this.sortDirection === ASCENDING ? -1 : 1;
    if (firstValue > secondValue) return this.sortDirection === ASCENDING ? 1 : -1;
    return 0;
  });

  this.refreshPagination();
}

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) {
      return '';
    }
    return this.sortDirection === ASCENDING ? 'bi bi-arrow-up' : 'bi bi-arrow-down';
  }
}
