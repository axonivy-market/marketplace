import type { MockedObject } from 'vitest';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick
} from '@angular/core/testing';
import { LogViewerComponent } from './logs-viewer.component';
import { LogStreamService } from '../../../core/services/logging/log-stream.service';
import { LogService } from '../log.service';
import { of, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { LogFileModel } from '../../../shared/models/apis/log-file-response.model';
import { TranslateModule } from '@ngx-translate/core';

describe('LogViewerComponent', () => {
  let component: LogViewerComponent;
  let fixture: ComponentFixture<LogViewerComponent>;
  let logStreamServiceMock: MockedObject<LogStreamService>;
  let logServiceMock: MockedObject<LogService>;
  let mockLogsSignal = signal<string[]>([]);

  beforeEach(async () => {
    logStreamServiceMock = {
      connect: vi.fn().mockName('LogStreamService.connect'),
      disconnect: vi.fn().mockName('LogStreamService.disconnect'),
      clear: vi.fn().mockName('LogStreamService.clear'),
      logs: mockLogsSignal
    };
    logServiceMock = {
      getLogFiles: vi.fn().mockName('LogService.getLogFiles'),
      getLogFileContent: vi.fn().mockName('LogService.getLogFileContent')
    };

    await TestBed.configureTestingModule({
      imports: [LogViewerComponent, TranslateModule.forRoot()],
      providers: [
        { provide: LogStreamService, useValue: logStreamServiceMock },
        { provide: LogService, useValue: logServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LogViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initial State', () => {
    it('should have default signal values', () => {
      expect(component.activeTab()).toBe('runtime-log');
      expect(component.autoScroll()).toBe(true);
      expect(component.isConnected()).toBe(false);
      expect(component.parsedLogs()).toEqual([]);
    });
  });

  describe('Log Parsing', () => {
    it('should parse standard log format correctly', () => {
      const logLine = '2026-01-20 00:29:45 INFO  logger - Application started';
      mockLogsSignal.set([logLine]);
      fixture.detectChanges();

      const parsed = component.parsedLogs()[0];
      expect(parsed.timestamp).toBe('2026-01-20 00:29:45');
      expect(parsed.level).toBe('INFO');
      expect(parsed.message).toBe('logger - Application started');
      expect(parsed.isLong).toBe(false);
    });

    it('should identify long logs', () => {
      const longMessage = 'A'.repeat(200);
      const logLine = `2026-01-20 00:29:45 DEBUG logger - ${longMessage}`;
      mockLogsSignal.set([logLine]);
      fixture.detectChanges();

      const parsed = component.parsedLogs()[0];
      expect(parsed.isLong).toBe(true);
    });

    it('should fallback for non-standard log format', () => {
      const logLine = 'Just a random message';
      mockLogsSignal.set([logLine]);
      fixture.detectChanges();

      const parsed = component.parsedLogs()[0];
      expect(parsed.level).toBe('INFO');
      expect(parsed.message).toBe(logLine);
    });
  });

  describe('Connection Control', () => {
    it('should call connect and update state on start()', () => {
      component.start();
      expect(logStreamServiceMock.connect).toHaveBeenCalled();
      expect(component.isConnected()).toBe(true);
    });

    it('should call disconnect and update state on stop()', () => {
      component.isConnected.set(true);
      component.stop();
      expect(logStreamServiceMock.disconnect).toHaveBeenCalled();
      expect(component.isConnected()).toBe(false);
    });

    it('should toggle connection', () => {
      component.isConnected.set(false);
      component.toggleConnection();
      expect(component.isConnected()).toBe(true);
      component.toggleConnection();
      expect(component.isConnected()).toBe(false);
    });
  });

  describe('Other Controls', () => {
    it('should clear logs', () => {
      component.clear();
      expect(logStreamServiceMock.clear).toHaveBeenCalled();
    });

    it('should toggle auto-scroll', () => {
      expect(component.autoScroll()).toBe(true);
      component.toggleAutoScroll();
      expect(component.autoScroll()).toBe(false);
    });
  });

  describe('Log Item Expansion', () => {
    it('should toggle expand state for an index', () => {
      expect(component.isExpanded(1)).toBe(false);
      component.toggleExpand(1);
      expect(component.isExpanded(1)).toBe(true);
      component.toggleExpand(1);
      expect(component.isExpanded(1)).toBe(false);
    });
  });

  describe('Downloads', () => {
    beforeEach(() => {
      vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:url');
      vi.spyOn(window.URL, 'revokeObjectURL');
      vi.spyOn(document, 'createElement');
    });

    it('should trigger download for all logs', () => {
      mockLogsSignal.set(['log1', 'log2']);
      component.downloadLogs();
      expect(document.createElement).toHaveBeenCalledWith('a');
    });

    it('should trigger download for JSON', () => {
      mockLogsSignal.set(['2026-01-20 00:29:45 INFO msg']);
      fixture.detectChanges();
      component.downloadAsJSON();
      expect(document.createElement).toHaveBeenCalledWith('a');
    });

    it('should trigger download for CSV', () => {
      mockLogsSignal.set(['2026-01-20 00:29:45 INFO msg']);
      fixture.detectChanges();
      component.downloadAsCSV();
      expect(document.createElement).toHaveBeenCalledWith('a');
    });
  });

  describe('Log Files Tab', () => {
    const mockFiles: LogFileModel[] = [
      { fileName: 'app.2026-02-12.log', size: 1024, date: '2026-02-12' }
    ];

    it('should load log files on tab change', () => {
      logServiceMock.getLogFiles.mockReturnValue(of(mockFiles));
      component.activeTab.set('logs-file');
      fixture.detectChanges();

      expect(logServiceMock.getLogFiles).toHaveBeenCalled();
      expect(component.logFiles()).toEqual(mockFiles);
    });

    it('should filter logs by date', () => {
      logServiceMock.getLogFiles.mockReturnValue(of(mockFiles));
      const event = { target: { value: '2026-02-12' } } as unknown as Event;
      component.filterLogsByDate(event);

      expect(component.selectedDate()).toBe('2026-02-12');
      expect(logServiceMock.getLogFiles).toHaveBeenCalledWith('2026-02-12');
    });

    it('should clear date filter', () => {
      logServiceMock.getLogFiles.mockReturnValue(of(mockFiles));
      component.selectedDate.set('2026-02-12');
      component.clearDateFilter();

      const today = new Date();
      const expectedDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

      expect(component.selectedDate()).toBe(expectedDate);
      expect(logServiceMock.getLogFiles).toHaveBeenCalledWith(expectedDate);
    });

    it('should trigger log file download', () => {
      const mockEvent = {
        stopPropagation: vi.fn().mockName('Event.stopPropagation')
      };
      component.downloadLogFile(mockFiles[0], mockEvent);

      expect(mockEvent.stopPropagation).toHaveBeenCalled();
      expect(logServiceMock.getLogFileContent).toHaveBeenCalledWith(
        mockFiles[0].fileName
      );
    });

    it('should handle error when loading files', () => {
      logServiceMock.getLogFiles.mockReturnValue(
        throwError(() => new Error('Error'))
      );
      component.activeTab.set('logs-file');
      fixture.detectChanges();

      expect(component.logFiles()).toEqual([]);
    });
  });

  describe('Utility Methods', () => {
    it('should return correct colors for log levels', () => {
      expect(component.getLogLevelColor('INFO')).toBe('#7ed957');
      expect(component.getLogLevelColor('ERROR')).toBe('#ffb6c1');
      expect(component.getLogLevelColor('UNKNOWN')).toBe('#ffffff');
    });

    it('should format file size correctly', () => {
      expect(component.formatFileSize(0)).toBe('0 Bytes');
      expect(component.formatFileSize(1024)).toBe('1 KB');
      expect(component.formatFileSize(1048576)).toBe('1 MB');
    });
  });
});
