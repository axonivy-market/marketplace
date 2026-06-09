import { expect, test } from '@playwright/test';

const productsResponse = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: 'Amazon Comprehend'
        },
        shortDescriptions: {
          en: 'Amazon Comprehend description',
          de: 'Amazon Comprehend description'
        },
        logoUrl: 'https://example.test/logo-amazon.png',
        type: 'connector',
        tags: ['AI'],
        marketDirectory: 'market/connector/amazon-comprehend/',
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      },
      {
        id: 'a-trust',
        names: {
          en: 'A-Trust',
          de: 'A-Trust'
        },
        shortDescriptions: {
          en: 'A-Trust description',
          de: 'A-Trust description'
        },
        logoUrl: 'https://example.test/logo-a-trust.png',
        type: 'connector',
        tags: ['e-signature'],
        marketDirectory: 'market/connector/a-trust/',
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/a-trust?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 2,
    totalPages: 1,
    number: 0
  }
};

test.describe('Product listing page', () => {
  test('renders products from API in a real browser', async ({ page }) => {
    await page.route('**/api/product**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(productsResponse)
      });
    });

    await page.goto('/');

    const productCards = page.locator('.row.product-container > .product-card');
    await expect(productCards).toHaveCount(2);
    await expect(productCards.first()).toContainText(/amazon comprehend/i);
    await expect(productCards.nth(1)).toContainText(/a-trust/i);
    await expect(page.locator('.not-found-result')).toHaveCount(0);
  });

  test('shows empty-state when API returns no products', async ({ page }) => {
    await page.route('**/api/product**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          _embedded: { products: [] },
          _links: { self: { href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20' } },
          page: { size: 20, totalElements: 0, totalPages: 0, number: 0 }
        })
      });
    });

    await page.goto('/');

    await expect(page.locator('.row.product-container > .product-card')).toHaveCount(0);
    await expect(page.locator('.not-found-result')).toBeVisible();
  });
});
