import { Component, DestroyRef, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { LogStreamService } from '../../../core/services/logging/log-stream.service';

interface ParsedLog {
  timestamp: string;
  level: string;
  message: string;
  isLong: boolean;
}

@Component({
  selector: 'app-log-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logs-viewer.component.html',
  styleUrls: ['./logs-viewer.component.scss']
})
export class LogViewerComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly logStream = inject(LogStreamService);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  readonly paused = signal(false);
  readonly autoScroll = signal(true);
  readonly isConnected = signal(false);
  readonly parsedLogs = signal<ParsedLog[]>([]);
  readonly logs = this.logStream.logs;
  readonly expandedLogs = signal<Set<number>>(new Set());

  constructor() {
    if (this.isBrowser) {
      effect(() => {
        if (this.autoScroll()) {
          queueMicrotask(() => this.scrollToBottom());
        }
      });

      // Watch for log changes and parse them
      effect(() => {
        const allLogs = this.logs();
        this.parsedLogs.set(allLogs.map(log => this.parseLog(log)));
      });
    }

    this.destroyRef.onDestroy(() => {
      this.logStream.disconnect();
    });
  }

  private parseLog(logLine: string): ParsedLog {
    // Format: 2026-01-20 00:29:45 INFO  logger - message
    const match = logLine.match(/^(\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2})\s+(\w+)\s+(.*)$/);
    if (match) {
      const message = match[3];
      return {
        timestamp: match[1],
        level: match[2].trim(),
        message: message,
        isLong: message.length > 150
      };
    }
    return {
      timestamp: new Date().toISOString(),
      level: 'INFO',
      message: logLine,
      isLong: logLine.length > 150
    };
  }

  toggleExpand(index: number): void {
    const current = new Set(this.expandedLogs());
    if (current.has(index)) {
      current.delete(index);
    } else {
      current.add(index);
    }
    this.expandedLogs.set(current);
  }

  isExpanded(index: number): boolean {
    return this.expandedLogs().has(index);
  }

  start(): void {
    this.logStream.connect();
    this.isConnected.set(true);
  }

  stop(): void {
    this.logStream.disconnect();
    this.isConnected.set(false);
  }

  toggleConnection(): void {
    if (this.isConnected()) {
      this.stop();
    } else {
      this.start();
    }
  }

  clear(): void {
    this.logStream.clear();
  }

  togglePause(): void {
    this.paused.update(v => !v);
  }

  toggleAutoScroll(): void {
    this.autoScroll.update(v => !v);
  }

  private scrollToBottom(): void {
    if (!this.isBrowser) return;

    const el = document.getElementById('log-container');
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }

  downloadLogs(): void {
    const logsText = this.logs().join('\n');
    this.downloadFile(logsText, 'logs.txt', 'text/plain');
  }

  downloadFilteredLogs(): void {
    const logsText = this.logs().join('\n');
    this.downloadFile(logsText, 'logs-filtered.txt', 'text/plain');
  }

  downloadAsJSON(): void {
    const jsonData = JSON.stringify(this.parsedLogs(), null, 2);
    this.downloadFile(jsonData, 'logs.json', 'application/json');
  }

  downloadAsCSV(): void {
    const headers = 'Timestamp,Level,Message\n';
    const csvData = this.parsedLogs()
      .map(log => `"${log.timestamp}","${log.level}","${log.message}"`)
      .join('\n');
    this.downloadFile(headers + csvData, 'logs.csv', 'text/csv');
  }

  private downloadFile(content: string, fileName: string, mimeType: string): void {
    const blob = new Blob([content], { type: mimeType });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  getLogLevelColor(level: string): string {
    const colors: { [key: string]: string } = {
      'DEBUG': '#ffffff',    // White
      'INFO': '#7ed957',     // Green
      'WARN': '#ffd966',     // Yellow
      'ERROR': '#ffb6c1',    // Pink
      'FATAL': '#ff6464'     // Red
    };
    return colors[level] || '#ffffff';
  }
}