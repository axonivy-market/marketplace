import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NAV_ITEMS } from '../../../constants/common.constant';
import { NavItem } from '../../../models/nav-item.model';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {
  @Input() navItems: NavItem[] = NAV_ITEMS;

  translateService = inject(TranslateService);
}
