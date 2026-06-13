import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { LogStreamService } from './log-stream.service';
import { RuntimeConfigService } from '../../configs/runtime-config.service';
import { RUNTIME_CONFIG_KEY } from '../../models/runtime-config';

describe('LogStreamService', () => {
  let service: LogStreamService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LogStreamService,
        RuntimeConfigService,
        {
          provide: PLATFORM_ID,
          useValue: 'browser'
        },
        {
          provide: RUNTIME_CONFIG_KEY,
          useValue: {
            apiUrl: '/app',
            githubOAuthAppClientId: '',
            githubOAuthCallback: '/auth/github/callback',
            githubApiUrl: '',
            dayInMiliseconds: 86400000,
            matomoSiteId: 0,
            matomoTrackerUrl: '',
            siblingNodeAppIp: '',
            allowedHosts: []
          }
        }
      ]
    });

    service = TestBed.inject(LogStreamService);
  });

  it('tracks task logs in memory', () => {
    (service as any).taskLogs.set(new Map([['syncProducts', ['line-1']]]));

    expect(service.getLogs('syncProducts')).toEqual(['line-1']);
    expect(service.hasLogs('syncProducts')).toBe(true);

    service.resetTask('syncProducts');

    expect(service.getLogs('syncProducts')).toEqual([]);
  });
});
