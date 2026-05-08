import { CommonModule } from "@angular/common";
import { Component, Input, inject } from "@angular/core";
import { TranslateModule, TranslateService } from "@ngx-translate/core";

@Component({
  standalone: true,
  selector: 'app-security-status-badge',
  templateUrl: './security-status-badge.component.html',
  styleUrls: ['./security-status-badge.component.scss'],
  imports: [CommonModule, TranslateModule]
})
export class SecurityStatusBadgeComponent {
  translateService = inject(TranslateService);
  @Input() status: string | null = null;
  @Input() alerts: Record<string, number> | null = null;
  @Input() alertCount: number | null = null;
  @Input() enabled: boolean | null = null;

  hasAlerts = (alerts: Record<string, number>) => alerts && Object.values(alerts).some(v => v > 0);
  alertKeys = (alerts: Record<string, number>) => Object.keys(alerts).filter(k => alerts[k] > 0);
}