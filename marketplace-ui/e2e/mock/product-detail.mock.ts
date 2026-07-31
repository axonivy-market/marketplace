import type { Page } from '@playwright/test';
import type { FeedbackApiResponse } from '../../src/app/shared/models/apis/feedback-response.model';
import type { ProductDetail } from '../../src/app/shared/models/product-detail.model';
import type { ProductReleasesApiResponse } from '../../src/app/shared/models/apis/product-releases-response.model';
import type { VersionData } from '../../src/app/shared/models/vesion-artifact.model';

const PRODUCT_ID = 'smart-workflow';
export const SMART_WORKFLOW_LATEST_VERSION = '14.0.0-SNAPSHOT';
export const SMART_WORKFLOW_BEST_MATCH_VERSION = '13.2.0';

const SMART_WORKFLOW_DESCRIPTION = [
  '# Smart Workflow',
  '',
  'Smart Workflow brings AI directly into Axon Ivy, so developers can build, run, and improve AI agents inside existing Axon processes.',
  '',
  'The platform helps teams automate work across business systems and streamline process execution.'
].join('\n');

const SMART_WORKFLOW_DEMO = [
  '## Demo',
  '',
  'This demo shows how Smart Workflow can orchestrate AI-powered process steps inside Axon Ivy.',
  '',
  '1. Start the demo process',
  '2. Review the generated output',
  '3. Continue the workflow with the result'
].join('\n');

const SMART_WORKFLOW_SETUP = [
  '## Installation Guide',
  '',
  'Use the installation guide to configure the connector and start the demo.',
  '',
  '1. Download the product package',
  '2. Add the required variables',
  '3. Run the project in Axon Ivy Designer'
].join('\n');

function createProductDetail(version = SMART_WORKFLOW_BEST_MATCH_VERSION): ProductDetail {
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
        en: SMART_WORKFLOW_DESCRIPTION,
        de: SMART_WORKFLOW_DESCRIPTION
      },
      demo: {
        en: SMART_WORKFLOW_DEMO,
        de: SMART_WORKFLOW_DEMO
      },
      setup: {
        en: SMART_WORKFLOW_SETUP,
        de: SMART_WORKFLOW_SETUP
      },
      component: null,
      isDependency: false,
      name: 'Smart Workflow',
      groupId: 'com.axonivy.market',
      artifactId: 'smart-workflow',
      type: 'iar',
      productId: PRODUCT_ID
    },
    installationCount: 42,
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

function createVersionData(version: string): VersionData {
  return {
    version,
    artifactsByVersion: [
      {
        value: 'smart-workflow-guide',
        label: 'Smart Workflow Guide',
        name: 'Smart Workflow Guide',
        downloadUrl: `https://example.test/${PRODUCT_ID}/${version}/smart-workflow-guide.zip`,
        isProductArtifact: true
      }
    ]
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
        body: SMART_WORKFLOW_BEST_MATCH_VERSION
      });
      return;
    }

    if (pathname.endsWith('/bestmatch')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(createProductDetail(SMART_WORKFLOW_BEST_MATCH_VERSION))
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
        body: JSON.stringify([
          createVersionData(SMART_WORKFLOW_BEST_MATCH_VERSION),
          createVersionData(SMART_WORKFLOW_LATEST_VERSION)
        ])
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
        version: SMART_WORKFLOW_BEST_MATCH_VERSION,
        artifactId: 'smart-workflow-guide',
        artifactName: 'Smart Workflow Guide',
        relativeLink: '/market-cache/smart-workflow/smart-workflow-guide/13.2.0/doc/index.html'
      })
    });
  });

  await page.route(`**/api/product-marketplace-data/installation-count/${PRODUCT_ID}`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(42)
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
