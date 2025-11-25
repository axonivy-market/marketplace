// Side menu component: collapsible like YouTube
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { HeaderComponent } from "../header/header.component";

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
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './side-menu.component.html',
  styleUrls: ['./side-menu.component.scss']
})
export class SideMenuComponent {
  // Sidebar open state (persisted in localStorage)
  isOpen = false;

  menu: MenuItem[] = [
    { icon: '📊', label: 'Sync jobs', route: '/octopus' },
    { icon: '📊', label: 'Security Monitor', route: '/octopus/security-monitor' },
    { icon: '📊', label: 'Feedback Approval', route: '/octopus/feedback-approval' },
    { icon: '📊', label: 'Sorting', route: '/octopus/sorting' },
    { icon: '⚙️', label: 'Quick Access', route: '/octopus/settings' }
  ];

  toggleSidebar() {
    this.isOpen = !this.isOpen;
    try { localStorage.setItem('sidebarOpen', String(this.isOpen)); } catch {}
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
}
