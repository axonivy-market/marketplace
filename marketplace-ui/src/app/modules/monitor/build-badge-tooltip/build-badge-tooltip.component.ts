import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  CI_BUILD,
  DEV_BUILD,
  E2E_BUILD,
  MONITORING_WIKI_LINK
} from '../../../shared/constants/common.constant';

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
  monitoringWikiLink = MONITORING_WIKI_LINK;
  //TODO: Please refactor this one with pipeline after Anais finish mock up
  ciTooltipPath = 'common.monitor.buildTooltip.ci';
  devTooltipPath = 'common.monitor.buildTooltip.dev';
  e2eTooltipPath = 'common.monitor.buildTooltip.e2e';

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

      case E2E_BUILD:
        this.tooltipContent = this.translateService.instant(
          this.e2eTooltipPath
        );
        break;

      default:
        break;
    }
  }
}
