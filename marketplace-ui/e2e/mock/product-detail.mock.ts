import type { Page } from '@playwright/test';
import type { FeedbackApiResponse } from '../../src/app/shared/models/apis/feedback-response.model';
import type { ProductDetail } from '../../src/app/shared/models/product-detail.model';
import type { ProductReleasesApiResponse } from '../../src/app/shared/models/apis/product-releases-response.model';

const PRODUCT_ID = 'smart-workflow';
export const SMART_WORKFLOW_LATEST_VERSION = '14.0.0-SNAPSHOT';

function createProductDetail(version = SMART_WORKFLOW_LATEST_VERSION): ProductDetail {
  return {
    id: PRODUCT_ID,
    names: {
      en: 'Smart Workflow',
      de: 'Smart Workflow'
    },
    shortDescriptions: {
      en: 'Smart Workflow automates the steps between your business systems.',
      de: 'Smart Workflow automates the steps between your business systems.'
    },
    logoUrl: 'https://example.test/smart-workflow-logo.png',
    logoDarkUrl: 'https://example.test/smart-workflow-logo-dark.png',
    type: 'solution',
    tags: ['workflow', 'automation'],
    vendor: 'Axon Ivy AG',
    vendorUrl: 'https://www.axonivy.com',
    vendorImage: '/assets/images/misc/axonivy-logo-black.svg',
    vendorImageDarkMode: '/assets/images/misc/axonivy-logo.svg',
    platformReview: '4.8',
    newestReleaseVersion: SMART_WORKFLOW_LATEST_VERSION,
    cost: 'Free',
    sourceUrl: 'https://github.com/axonivy-market/smart-workflow',
    statusBadgeUrl: 'https://github.com/axonivy-market/smart-workflow/actions/workflows/ci.yml/badge.svg',
    language: 'English',
    industry: 'Cross-Industry',
    contactUs: false,
    productModuleContent: {
      version,
      description: {
        en: 'Smart Workflow description',
        de: 'Smart Workflow description'
      },
      demo: null,
      setup: null,
      component: null,
      isDependency: false,
      name: 'Smart Workflow',
      groupId: 'com.axonivy.market',
      artifactId: 'smart-workflow',
      type: 'iar',
      productId: PRODUCT_ID
    },
    installationCount: 0,
    mavenDropins: false,
    _links: {
      self: {
        href: `http://localhost:8080/api/product-details/${PRODUCT_ID}`
      }
    },
    isFocusedProduct: false
  };
}

function createEmptyFeedbackPage(): FeedbackApiResponse {
  return {
    _embedded: {
      feedbacks: []
    },
    _links: {
      self: {
        href: `http://localhost:8080/api/feedback/product/${PRODUCT_ID}?page=0&size=8&sort=newest`
      }
    },
    page: {
      size: 8,
      totalElements: 0,
      totalPages: 0,
      number: 0
    }
  };
}

function createEmptyReleasesPage(): ProductReleasesApiResponse {
  return {
    _embedded: {
      gitHubReleaseModelList: []
    },
    _links: {
      self: {
        href: `http://localhost:8080/api/product-details/${PRODUCT_ID}/releases?page=0&size=5`
      }
    },
    page: {
      size: 5,
      totalElements: 0,
      totalPages: 0,
      number: 0
    }
  };
}

export async function setupProductDetailMocks(page: Page): Promise<void> {
  await page.route(`**/api/product-details/${PRODUCT_ID}**`, async route => {
    const url = new URL(route.request().url());
    const pathname = url.pathname;

    if (pathname.endsWith('/best-match-version')) {
      await route.fulfill({
        status: 200,
        contentType: 'text/plain',
        body: SMART_WORKFLOW_LATEST_VERSION
      });
      return;
    }

    if (pathname.endsWith('/releases')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(createEmptyReleasesPage())
      });
      return;
    }

    if (pathname.endsWith('/versions')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
      return;
    }

    if (pathname.endsWith('/designerversions')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
      return;
    }

    if (pathname.includes('/artifact')) {
      await route.fulfill({
        status: 200,
        contentType: 'text/plain',
        body: ''
      });
      return;
    }

    const versionMatch = pathname.match(new RegExp(`^/api/product-details/${PRODUCT_ID}/([^/]+)$`));
    if (versionMatch) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(createProductDetail(versionMatch[1]))
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(createProductDetail())
    });
  });

  await page.route(`**/api/externaldocument/${PRODUCT_ID}**`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        productId: PRODUCT_ID,
        version: SMART_WORKFLOW_LATEST_VERSION,
        artifactId: 'smart-workflow-guide',
        artifactName: 'Smart Workflow Guide',
        relativeLink: '/market-cache/smart-workflow/smart-workflow-guide/14.0.0-SNAPSHOT/doc/index.html'
      })
    });
  });

  await page.route('**/api/feedback**', async route => {
    const url = new URL(route.request().url());
    const pathname = url.pathname;

    if (pathname === '/api/feedback/product/smart-workflow/rating') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
      return;
    }

    if (pathname === '/api/feedback/product/smart-workflow') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(createEmptyFeedbackPage())
      });
      return;
    }

    if (pathname === '/api/feedback') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([])
    });
  });
}
