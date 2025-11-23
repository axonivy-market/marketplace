import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { Observable, filter } from 'rxjs';
import { ScheduledTasksService } from './admin-dashboard.service';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';
import { SideMenuComponent } from '../../shared/components/side-menu/side-menu.component';

@Component({
  selector: 'app-scheduled-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule, SideMenuComponent, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminDashboardComponent implements OnInit {
  private readonly service = inject(ScheduledTasksService);
  private readonly router = inject(Router);
  readonly refreshMs = 15000;
  tasks$!: Observable<ScheduledTaskInfo[]>;
  showScheduledTasks = true; // only for base /octopus (sync jobs)

  ngOnInit() {
    this.tasks$ = this.service.pollWithImmediate(this.refreshMs);
    this.updateVisibility(this.router.url);
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      this.updateVisibility(this.router.url);
    });
  }

  formatDate(value?: string | null): string {
    if (!value) return '';
    const date = new Date(value);
    return date.toLocaleString();
  }

  private updateVisibility(url: string) {
    // Match exactly /octopus (optionally trailing slash or query params)
    this.showScheduledTasks = /^\/octopus\/?(\?.*)?$/.test(url);
  }
}
