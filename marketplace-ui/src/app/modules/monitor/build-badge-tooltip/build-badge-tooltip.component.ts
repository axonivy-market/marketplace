import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CI_BUILD, DEV_BUILD } from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-build-badge-tooltip',
  standalone: true,
  imports: [CommonModule, TranslateModule, NgbTooltipModule],
  templateUrl: './build-badge-tooltip.component.html',
  styleUrl: './build-badge-tooltip.component.scss'
})
export class BuildBadgeTooltipComponent implements OnInit {
  @Input() buildType = '';
  tooltipContent = '';
  monitoringWikiLink = 'https://github.com/axonivy-market/market/wiki';
  ciTooltipPath = 'monitor.buildTooltip.ci';
  devTooltipPath = 'monitor.buildTooltip.dev';

  translateService = inject(TranslateService);

  ngOnInit() {
    this.constructToolTipContent();
  }

  constructToolTipContent() {
    switch (this.buildType) {
      case CI_BUILD:
        this.tooltipContent = this.translateService.instant(this.ciTooltipPath);
        break;

      case DEV_BUILD:
        this.tooltipContent = this.translateService.instant(
          this.devTooltipPath
        );
        break;

      default:
        break;
    }
  }
}
