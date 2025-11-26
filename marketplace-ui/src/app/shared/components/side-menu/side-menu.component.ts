// Side menu component: collapsible like YouTube
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';

interface MenuItem {
  icon: string;
  label: string;
  route?: string;
  children?: MenuItem[];
  open?: boolean;
}

@Component({
  selector: 'app-side-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './side-menu.component.html',
  styleUrls: ['./side-menu.component.scss']
})
export class SideMenuComponent implements OnInit {
  // Sidebar open state (persisted in localStorage)
  isOpen = false;
  @Output() openChange = new EventEmitter<boolean>();

  menu: MenuItem[] = [
    { icon: 'bi bi-arrow-repeat', label: 'Sync jobs', route: '/octopus' },
    { icon: 'bi bi-shield-shaded', label: 'Security Monitor', route: '/octopus/security-monitor' },
    { icon: 'bi bi-chat-dots', label: 'Feedback Approval', route: '/octopus/feedback-approval' },
    { icon: 'bi bi-sort-alpha-up', label: 'Sorting', route: '/octopus/sorting' },
    { icon: 'bi bi-skip-forward', label: 'Quick Access', route: '/octopus/quick-access' }
  ];

  toggleSidebar() {
    this.setOpen(!this.isOpen);
  }

  toggleSubmenu(item: MenuItem) {
    if (!item.children) return;
    item.open = !item.open;
  }

  constructor(private readonly router: Router) {
    try {
      const stored = localStorage.getItem('sidebarOpen');
      if (stored !== null) this.isOpen = stored === 'true';
    } catch {}
    // Debug: log successful navigations (optional)
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        // navigation end hook (keep minimal to satisfy linter)
      }
    });
  }

  ngOnInit(): void {
    this.emitChange();
  }

  setOpen(open: boolean): void {
    if (this.isOpen === open) {
      return;
    }
    this.isOpen = open;
    this.persistState();
    this.emitChange();
  }

  private persistState(): void {
    try {
      localStorage.setItem('sidebarOpen', String(this.isOpen));
    } catch {}
  }

  private emitChange(): void {
    this.openChange.emit(this.isOpen);
  }
}
