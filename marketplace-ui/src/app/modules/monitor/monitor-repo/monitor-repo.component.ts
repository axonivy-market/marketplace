import { Component, EventEmitter, inject, Input, Output, PLATFORM_ID } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { WorkflowIconPipe } from "../../../shared/pipes/workflow-icon.pipe";
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildBadgeTooltipComponent } from '../build-badge-tooltip/build-badge-tooltip.component';
import {
  CI_BUILD,
  DEV_BUILD,
  MONITORING_WIKI_LINK
} from '../../../shared/constants/common.constant';
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
    BuildStatusEntriesPipe,
    WorkflowIconPipe,
    IsEmptyObjectPipe,
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
  @Input() activeTab2 = {
  focused: {
    default: true,
    report: false
  },
  standard: {
    default: false,
    report: true
  }
};

  @Input() focusedTab = {
    default: true,
    report: false
  };
  
  @Input() standardTab = {
    default: false,
    report: true
  };

  @Output() searchChange = new EventEmitter<string>();
  // @Input() sortable: SortColumn = '';
  // @Input() direction: SortDirection = '';
  @Output() sort = new EventEmitter<SortEvent>();
  mode: { [key: string]: 'default' | 'report' } = {
  focused: 'default',
  standard: 'default'
};

//   mode2: { [key: string]: {
//     default: boolean,
//     report: boolean
//   }} = {
//   focused: {
//     default: true,
//     report: false
//   },
//   standard: {
//     default: false,
//     report: true
//   }
// };


  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  translateService = inject(TranslateService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);
  pageTitleService: PageTitleService = inject(PageTitleService);

  ciBuild = CI_BUILD;
  devBuild = DEV_BUILD;
  searchTextChanged = new Subject<string>();
  searchText = '';
  page = 1;
	pageSize = 10;
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
