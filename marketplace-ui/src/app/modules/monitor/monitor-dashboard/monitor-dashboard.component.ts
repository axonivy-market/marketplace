import { Component, inject, OnInit, PLATFORM_ID, EventEmitter,Output} from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { SortOptionLabel } from '../../../shared/enums/sort-option.enum';
import { SORT_MONITOR_OPTION } from '../../../shared/constants/common.constant';
import { ItemDropdown } from '../../../shared/models/item-dropdown.model';
import { Observable } from 'rxjs';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule, CommonDropdownComponent],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  @Output() sortChange = new EventEmitter<SortOptionLabel>();

  repositories: Repository[] = [];
  loading = true;
  error = '';
  isReloading = false;

  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);

  sorts: ItemDropdown<SortOptionLabel>[] = SORT_MONITOR_OPTION;
  selectedSort: ItemDropdown<SortOptionLabel> = this.sorts[0];
  selectedSortLabel: string = this.selectedSort.label;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.fetchRepositoriesBySort(this.selectedSort.value);
    } else {
      this.loading = false;
    }
  }

  fetchRepositoriesBySort(sortType: SortOptionLabel): void {
    this.loading = true;
    let fetch$: Observable<Repository[]>;

    if (sortType === SortOptionLabel.FOCUSED) {
      fetch$ = this.githubService.getFocusedRepositories();
    } else {
      fetch$ = this.githubService.getStandardRepositories();
    }

    fetch$.subscribe({
      next: data => {
        this.repositories = data;
        this.loading = false;
      },
      error: err => {
        this.error = err.message || 'Failed to load repositories.';
        this.loading = false;
      }
    });
  }

  getTestCount(repo: Repository, workflow: string, environment: string, status: string): number {
    if (!repo.testResults) {
      return 0;
    }
    const result = repo.testResults.find(test =>
      test.workflow === workflow.toUpperCase() &&
      test.environment === environment.toUpperCase() &&
      test.status === status.toUpperCase()
    );
    if (result) {
      return result.count;
    } else {
      return 0;
    }
  }

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/report', repo, upperWorkflow]);
  }

  onSortChange(sortValue: SortOptionLabel): void {
    const found = this.sorts.find(s => s.value === sortValue);
    if (found) {
      this.selectedSort = found;
      this.selectedSortLabel = found.label;
      this.fetchRepositoriesBySort(sortValue);
      this.sortChange.emit(sortValue);
    }
  }
}
