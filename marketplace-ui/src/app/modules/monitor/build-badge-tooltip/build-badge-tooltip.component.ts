import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  CI_BUILD,
  DEV_BUILD,
  E2E_BUILD,
  MONITORING_WIKI_LINK
} from '../../../shared/constants/common.constant';
import { startWith } from 'rxjs';

@Component({
  selector: 'app-build-badge-tooltip',
  imports: [TranslateModule, NgbTooltipModule],
  templateUrl: './build-badge-tooltip.component.html',
  styleUrl: './build-badge-tooltip.component.scss'
})
export class BuildBadgeTooltipComponent implements OnInit {
  @Input() buildType = '';
  tooltipContent = '';
  monitoringWikiLink = MONITORING_WIKI_LINK;
  ciTooltipPath = 'common.monitor.buildTooltip.ci';
  devTooltipPath = 'common.monitor.buildTooltip.dev';
  e2eTooltipPath = 'common.monitor.buildTooltip.e2e';

  translateService = inject(TranslateService);

  ngOnInit() {
    this.translateService.onLangChange
      .pipe(startWith(null))
      .subscribe(() => this.constructToolTipContent());
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
