import {
  Component,
  DestroyRef,
  effect,
  inject,
  PLATFORM_ID,
  signal
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { LogStreamService } from '../../../core/services/logging/log-stream.service';
import { LogFileModel } from '../../../shared/models/apis/log-file-response.model';
import { LogService } from '../log.service';

interface ParsedLog {
  timestamp: string;
  level: string;
  message: string;
  isLong: boolean;
}

const LOG_LINE_REGEX =
  /^(?<timestamp>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2})\s+(?<level>\w+)\s+(?<message>.*)$/;
const LONG_MESSAGE_THRESHOLD = 150;

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
  private readonly logService = inject(LogService);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  readonly activeTab = signal<'runtime-log' | 'logs-file'>('runtime-log');
  readonly paused = signal(false);
  readonly autoScroll = signal(true);
  readonly isConnected = signal(false);
  readonly parsedLogs = signal<ParsedLog[]>([]);
  readonly logs = this.logStream.logs;
  readonly expandedLogs = signal<Set<number>>(new Set());
  readonly selectedDate = signal<string>('');
  readonly logFiles = signal<LogFileModel[]>([]);
  readonly filteredLogFiles = signal<LogFileModel[]>([]);

  constructor() {
    if (this.isBrowser) {
      effect(() => {
        this.parsedLogs();
        if (this.autoScroll()) {
          queueMicrotask(() => this.scrollToBottom());
        }
      });
      effect(() => {
        const allLogs = this.logs();
        this.parsedLogs.set(allLogs.map(log => this.parseLog(log)));
      });
      effect(() => {
        const tab = this.activeTab();
        if (tab === 'logs-file') {
          this.loadLogFiles();
        }
      });
    }

    this.destroyRef.onDestroy(() => {
      this.logStream.disconnect();
    });
  }

  private parseLog(logLine: string): ParsedLog {
    // Format: 2026-01-20 00:29:45 INFO  logger - message
    const match = LOG_LINE_REGEX.exec(logLine);
    if (match?.groups) {
      const { timestamp, level, message } = match.groups;
      return {
        timestamp,
        level: level.trim(),
        message,
        isLong: message.length > LONG_MESSAGE_THRESHOLD
      };
    }
    return {
      timestamp: new Date().toISOString(),
      level: 'INFO',
      message: logLine,
      isLong: logLine.length > LONG_MESSAGE_THRESHOLD
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
    if (!this.isBrowser) {
      return;
    }

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

  private downloadFile(
    content: string,
    fileName: string,
    mimeType: string
  ): void {
    const blob = new Blob([content], { type: mimeType });
    const url = globalThis.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    globalThis.URL.revokeObjectURL(url);
  }

  getLogLevelColor(level: string): string {
    const colors: { [key: string]: string } = {
      DEBUG: '#ffffff', // White
      INFO: '#7ed957', // Green
      WARN: '#ffd966', // Yellow
      ERROR: '#ffb6c1', // Pink
      FATAL: '#ff6464' // Red
    };
    return colors[level] || '#ffffff';
  }

  private extractDateFromFilename(filename: string): string {
    // Extract date from filename format: application.2026-02-12.log.gz
    const dateRegex = /(\d{4}-\d{2}-\d{2})/;
    const match = dateRegex.exec(filename);
    return match ? match[1] : '';
  }

  private loadLogFiles(): void {
    const selectedDate = this.selectedDate();
    this.logService.getLogFiles(selectedDate).subscribe({
      next: (response: LogFileModel[]) => {
        this.logFiles.set(response);
        this.filteredLogFiles.set(response);
      },
      error: _error => {
        this.logFiles.set([]);
        this.filteredLogFiles.set([]);
      }
    });
  }

  filterLogsByDate(event: Event): void {
    const target = event.target as HTMLInputElement;
    const selectedDate = target.value;
    this.selectedDate.set(selectedDate);
    this.loadLogFiles();
  }

  clearDateFilter(): void {
    this.selectedDate.set('');
    this.loadLogFiles();
  }

  downloadLogFile(logFile: LogFileModel, event: Event): void {
    event.stopPropagation();
    this.logService.getLogFileContent(logFile.fileName);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) {
      return '0 Bytes';
    }
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${Math.round((bytes / Math.pow(k, i)) * 100) / 100} ${sizes[i]}`;
  }
}
