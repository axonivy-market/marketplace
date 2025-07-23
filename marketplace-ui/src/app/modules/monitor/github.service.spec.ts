
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GithubService, Repository, TestResult, TestStep } from './github.service';
import { API_URI } from '../../shared/constants/api.constant';

const mockRepos: Repository[] = [
  {
    name: 'repo1',
    htmlUrl: 'https://github.com/user/repo1',
    language: 'TypeScript',
    lastUpdated: '2025-07-20T12:00:00Z',
    ciBadgeUrl: 'https://example.com/badge/ci.svg',
    devBadgeUrl: 'https://example.com/badge/dev.svg',
    testResults: [
      { environment: 'ALL', workflow: 'CI', count: 10, status: 'PASSED' },
      { environment: 'MOCK', workflow: 'CI', count: 5, status: 'PASSED' }
    ]
  }
];

const mockTestStep: TestStep = {
  name: 'Step 1',
  status: 'PASSED',
  type: 'unit',
  testType: 'mock'
};

describe('GithubService', () => {
  let service: GithubService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GithubService]
    });
    service = TestBed.inject(GithubService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch repositories', () => {
    service.getRepositories().subscribe(repos => {
      expect(repos).toEqual(mockRepos);
    });
    const req = httpMock.expectOne(API_URI.GITHUB_REPOS);
    expect(req.request.method).toBe('GET');
    req.flush(mockRepos);
  });

  it('should sync github repositories', () => {
    service.syncGithubRepos().subscribe(repos => {
      expect(repos).toEqual(mockRepos);
    });
    const req = httpMock.expectOne(API_URI.SYNC_GITHUB_REPOS);
    expect(req.request.method).toBe('GET');
    req.flush(mockRepos);
  });

  it('should fetch test report', () => {
    service.getTestReport('repo1', 'CI').subscribe(step => {
      expect(step).toEqual(mockTestStep);
    });
    const req = httpMock.expectOne(`${API_URI.GITHUB_REPORT}/repo1/CI`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTestStep);
  });
});
