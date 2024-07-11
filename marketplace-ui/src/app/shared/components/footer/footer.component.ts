import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import {
  IVY_FOOTER_LINKS,
  NAV_ITEMS,
  SOCIAL_MEDIA_LINK
} from '../../constants/common.constant';
import { NavItem } from '../../models/nav-item.model';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss', '../../../app.component.scss']
})
export class FooterComponent {
  themeService = inject(ThemeService);
  socialMediaLinks = SOCIAL_MEDIA_LINK;
  navItems: NavItem[] = NAV_ITEMS;
  ivyFooterLinks = IVY_FOOTER_LINKS;
}
