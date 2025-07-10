import { Component, OnInit } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { MONITORING_REDIRECT_URL } from '../../constants/common.constant';

@Component({
  selector: 'app-monitoring-redirect',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>"

})
export class MonitoringRedirectComponent implements OnInit {
  protected window = window;

  ngOnInit(): void {
    this.window.location.href = MONITORING_REDIRECT_URL;
  }
}