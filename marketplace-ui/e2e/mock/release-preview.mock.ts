import type { Page } from '@playwright/test';
import type { ReleasePreviewData } from '../../src/app/shared/models/release-preview-data.model';

export const RELEASE_PREVIEW_FIXTURE_ZIP = 'e2e/fixtures/asana-connector-product-13.2.0-SNAPSHOT.zip';

export const ASANA_RELEASE_PREVIEW_RESPONSE: ReleasePreviewData = {
  description: {
    en: [
      '# Asana Connector',
      '',
      '[Asana](https://asana.com/) is a web and mobile application designed for team collaboration and project management.',
      'The Axon Ivy Asana connector provides the following capabilities:',
      '- Create a task',
      '- Retrieve task details',
      '- Update task details',
      '- Delete a task'
    ].join('\n')
  },
  demo: {
    en: [
      '## Demo',
      '',
      'This demo provides the following features:',
      '',
      '1. Create Task',
      '- Creates a new task with sample data.',
      '2. Task List',
      '- Shows a table of tasks based on the selected Workspace and Project.',
      '3. Update Task',
      '- Allows editing task details such as the task name, assignee, start date, and due date.'
    ].join('\n')
  },
  setup: {
    en: [
      '## Setup',
      '',
      'In order to use this product you must configure the variables.',
      '',
      'Add the following code block to your `config/variables.yaml` file.',
      '',
      '### Asana Registration',
      '',
      '1. Register for an Asana account on the Asana Dashboard.',
      '1. Create a PAT that you will later add to `variables.yaml`.',
      '1. Refer to the Quick start guide to learn how to access your Workspace GID.'
    ].join('\n')
  },
  component: {
    en: ''
  }
};

export async function setupReleasePreviewMock(page: Page): Promise<void> {
  await page.route('**/api/release-preview', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ASANA_RELEASE_PREVIEW_RESPONSE)
    });
  });
}
