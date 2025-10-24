import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import {
  GithubService,
  Repository,
  RepositoryPages,
  TestStep
} from './github.service';
import { API_URI } from '../../shared/constants/api.constant';
import { MonitoringCriteria } from '../../shared/models/criteria.model';

const mockRepos: Repository[] = [
  {
    repoName: 'repo1',
    productId: 'id1',
    htmlUrl: 'https://github.com/user/repo1',
    workflowInformation:[],
    focused: true,
    testResults: []
  }
];

const mockTestStep: TestStep = {
  name: 'Step 1',
  status: 'PASSED',
  type: 'unit'
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

  const mockCriteria: MonitoringCriteria = {
    search: 'test',
    isFocused: 'true',
    sortDirection: 'ASC',
    workflowType: 'CI',
    pageable: { page: 0, size: 10 }
  };

  const mockResponse: RepositoryPages = {
    _embedded: {
      githubRepos: [
        {
          repoName: 'repo-1',
          productId: 'product-1',
          htmlUrl: 'http://github.com/repo-1',
          focused: true,
          workflowInformation: [],
          testResults: []
        }
      ]
    },
    page: {
      size: 10,
      totalElements: 1,
      totalPages: 1,
      number: 0
    }
  };

  it('should call getRepositories and return RepositoryPages', () => {
    service.getRepositories(mockCriteria).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne((request) => {
      return (
        request.url === API_URI.MONITOR_DASHBOARD &&
        request.params.get('search') === mockCriteria.search &&
        request.params.get('isFocused') === mockCriteria.isFocused &&
        request.params.get('sortDirection') === mockCriteria.sortDirection &&
        request.params.get('workflowType') === mockCriteria.workflowType &&
        request.params.get('page') === mockCriteria.pageable.page.toString() &&
        request.params.get('size') === mockCriteria.pageable.size.toString()
      );
    });

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should fetch test report', () => {
    service.getTestReport('repo1', 'CI').subscribe(step => {
      expect(step).toEqual(mockTestStep);
    });
    const req = httpMock.expectOne(`${API_URI.GITHUB_REPORT}/repo1/CI`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTestStep);
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
