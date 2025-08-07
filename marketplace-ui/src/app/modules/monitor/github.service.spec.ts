
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GithubService, Repository, TestStep } from './github.service';
import { API_URI } from '../../shared/constants/api.constant';

const mockRepos: Repository[] = [
  {
    name: 'repo1',
    language: 'TypeScript',
    lastUpdated: '2025-07-20T12:00:00Z',
    focused: true,
    testResults: []
  }
];

const mockTestStep: TestStep = {
  name: 'Step 1',
  status: 'PASSED',
  type: 'unit',
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
    const req = httpMock.expectOne(API_URI.MONITOR_DASHBOARD);
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

  it('should handle error when fetching repositories', () => {
    let error: any;
    service.getRepositories().subscribe({
      next: () => {},
      error: err => error = err
    });
    const req = httpMock.expectOne(API_URI.MONITOR_DASHBOARD);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
    expect(error).toBeTruthy();
  });


  it('should handle error when fetching test report', () => {
    let error: any;
    service.getTestReport('repo1', 'CI').subscribe({
      next: () => {},
      error: err => error = err
    });
    const req = httpMock.expectOne(`${API_URI.GITHUB_REPORT}/repo1/CI`);
    req.flush('Error', { status: 404, statusText: 'Not Found' });
    expect(error).toBeTruthy();
  });

  it('should call getTestReport with different workflow', () => {
    service.getTestReport('repo1', 'DEV').subscribe(step => {
      expect(step).toEqual(mockTestStep);
    });
    const req = httpMock.expectOne(`${API_URI.GITHUB_REPORT}/repo1/DEV`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTestStep);
  });
});