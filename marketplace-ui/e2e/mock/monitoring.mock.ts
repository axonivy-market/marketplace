import type { Page } from '@playwright/test';
import type { Repository, RepositoryPages } from '../../src/app/modules/monitor/github.service';
import { ALL_ITEMS_PAGE_SIZE } from '../../src/app/shared/constants/common.constant';

function createRepository(index: number): Repository {
  const repoNumber = index + 1;
  const repoName = repoNumber === 1 ? 'smart-workflow' : repoNumber === 2 ? 'persistence-utils' : `repo-${repoNumber}`;
  const htmlUrl = `https://market.axonivy.com/${repoName}`;
  const baseDate = new Date(Date.UTC(2026, 6, 14, index % 24, 0, 0));

  return {
    repoName,
    productId: repoName,
    htmlUrl,
    focused: repoNumber <= 2,
    workflowInformation: [
      {
        workflowType: 'CI',
        lastBuilt: baseDate,
        conclusion: repoNumber === 2 ? 'failure' : 'success',
        lastBuiltRunUrl: `${htmlUrl}/ci`,
        currentWorkflowState: 'active',
        disabledDate: null
      },
      {
        workflowType: 'DEV',
        lastBuilt: new Date(baseDate.getTime() + 10 * 60 * 1000),
        conclusion: 'success',
        lastBuiltRunUrl: `${htmlUrl}/dev`,
        currentWorkflowState: 'active',
        disabledDate: null
      },
      {
        workflowType: 'E2E',
        lastBuilt: new Date(baseDate.getTime() + 20 * 60 * 1000),
        conclusion: 'success',
        lastBuiltRunUrl: `${htmlUrl}/e2e`,
        currentWorkflowState: 'active',
        disabledDate: null
      }
    ],
    testResults: [
      {
        workflow: 'CI',
        results: { PASSED: repoNumber + 8, FAILED: repoNumber === 2 ? 1 : 0, SKIPPED: 0 }
      },
      {
        workflow: 'DEV',
        results: { PASSED: repoNumber + 9, FAILED: 0, SKIPPED: 0 }
      },
      {
        workflow: 'E2E',
        results: { PASSED: repoNumber + 6, FAILED: 0, SKIPPED: repoNumber % 2 }
      }
    ]
  };
}

export const MONITORING_REPOSITORIES: Repository[] = Array.from({ length: 25 }, (_, index) => createRepository(index));

function buildMonitoringPage(pageNumber: number, pageSize: number): RepositoryPages {
  return buildMonitoringPageWithSearch(pageNumber, pageSize, '');
}

function buildMonitoringPageWithSearch(pageNumber: number, pageSize: number, search: string): RepositoryPages {
  const normalizedSearch = search.trim().toLowerCase();
  const filteredRepositories = normalizedSearch
    ? MONITORING_REPOSITORIES.filter(repo => repo.repoName.toLowerCase().includes(normalizedSearch))
    : MONITORING_REPOSITORIES;
  const effectivePageSize = pageSize === ALL_ITEMS_PAGE_SIZE ? MONITORING_REPOSITORIES.length : pageSize;
  const start = pageNumber * effectivePageSize;
  const end = start + effectivePageSize;
  const pagedRepositories = filteredRepositories.slice(start, end);

  return {
    _embedded: {
      githubRepos: pagedRepositories
    },
    page: {
      size: effectivePageSize,
      totalElements: filteredRepositories.length,
      totalPages: Math.ceil(filteredRepositories.length / effectivePageSize),
      number: pageNumber
    }
  } as RepositoryPages;
}

export const MONITORING_PAGE = buildMonitoringPage(0, 10);

export async function setupMonitoringApiMocks(page: Page): Promise<void> {
  await page.route('**/api/monitor-dashboard**', async route => {
    const url = new URL(route.request().url());
    const pageNumber = Number(url.searchParams.get('page') ?? '0');
    const pageSize = Number(url.searchParams.get('size') ?? '10');
    const search = url.searchParams.get('search') ?? '';

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(buildMonitoringPageWithSearch(pageNumber, pageSize, search))
    });
  });
}
