import { Component, EventEmitter, inject, Input, Output, PLATFORM_ID } from '@angular/core';
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
import { Subject } from 'rxjs';
import { RepoTestResultComponent } from "../repo-test-result/repo-test-result.component";

export interface SortEvent {
  column: SortColumn;
  direction: SortDirection;
}

export type SortColumn = 'name' | 'rating' | 'status' | 'createdDate' | 'updatedDate' | 'moderator' | 'reviewDate';
export type SortDirection = 'asc' | 'desc' | '';
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
  @Input() activeTab = 'focused';
  @Output() searchChange = new EventEmitter<string>();
  // @Input() sortable: SortColumn = '';
  // @Input() direction: SortDirection = '';
  @Output() sort = new EventEmitter<SortEvent>();
  mode: { [key: string]: 'default' | 'report' } = {
    focused: 'default',
    standard: 'default'
  };

  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  translateService = inject(TranslateService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);
  pageTitleService: PageTitleService = inject(PageTitleService);

  searchTextChanged = new Subject<string>();
  searchText = '';
  page = 1;
  pageSize = 10;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  allRepositories: Repository[] = [];
  filteredRepositories: Repository[] = [];
  displayedRepositories: Repository[] = [];

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
    return `https://market.axonivy.com/${encodeURIComponent(repoName)}`;
  }

  sortTable(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.displayedRepositories.sort((a, b) => {
      const valA = a[column].toString().toLowerCase();
      const valB = b[column].toString().toLowerCase();

      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) return '';
    return this.sortDirection === 'asc' ? '▲' : '▼';
  }

  // onSort({ column, direction }: SortEvent) {
  // 	// resetting other headers
  // 	for (const header of this.headers) {
  // 		if (header.sortable !== column) {
  // 			header.direction = '';
  // 		}
  // 	}

  // 	// sorting countries
  // 	if (direction === '' || column === '') {
  // 		this.countries = COUNTRIES;
  // 	} else {
  // 		this.countries = [...COUNTRIES].sort((a, b) => {
  // 			const res = compare(a[column], b[column]);
  // 			return direction === 'asc' ? res : -res;
  // 		});
  // 	}
  // }
}
