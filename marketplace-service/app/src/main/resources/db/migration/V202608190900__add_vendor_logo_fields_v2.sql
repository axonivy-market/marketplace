UPDATE product
SET internal = TRUE
WHERE id IN (
    'portal',
    'web-tester',
    'visualvm-plugin',
    'ui-path-connector',
    'jsf-formarchive-util',
    'stateful-datatable-demo',
    'smart-workflow',
    'rule-engine',
    'process-inspector',
    'process-analyser',
    'persistence-utils',
    'pattern-demos',
    'master-detail-utils',
    'mailstore-utils',
    'ivy-load-test',
    'idp-utils',
    'html-dialog-utils',
    'html-dialog-demo',
    'gdpr-utils',
    'express-importer',
    'error-handling-demo',
    'email-encryption',
    'docfactory',
    'dmn-decision-table',
    'db-utils',
    'connectivity-demo',
    'coffee-machine-connector',
    'cms-live-editor',
    'case-process-viewer-utils',
    'case-mail-component-utils',
    'captcha-utils',
    'axonivy-words',
    'metaproc-utils',
    'axonivy-pdf',
    'form-editor-demo',
    'axonivy-express',
    'axonivy-cells',
    'aspose-email-demo',
    'aspose-barcode-demo',
    'approval-decision-utils',
    'anonymous-user-demos',
    'ai-assistant'
);

UPDATE product_marketplace_data
SET custom_order = NULL;

UPDATE product_marketplace_data
SET custom_order = 1
WHERE id = 'portal';

UPDATE product_marketplace_data
SET custom_order = 2
WHERE id = 'smart-workflow';

UPDATE product_marketplace_data
SET custom_order = 3
WHERE id = 'idp-utils';