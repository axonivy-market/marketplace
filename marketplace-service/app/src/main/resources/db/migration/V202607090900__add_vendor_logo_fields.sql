ALTER TABLE product ADD COLUMN IF NOT EXISTS vendor_logo VARCHAR(255) DEFAULT NULL;

ALTER TABLE product ADD COLUMN IF NOT EXISTS vendor_logo_dark_mode VARCHAR(255) DEFAULT NULL;

ALTER TABLE product ADD COLUMN IF NOT EXISTS internal BOOLEAN DEFAULT NULL;

UPDATE product
SET internal = TRUE
WHERE id IN (
    'portal',
    'webtester',
    'visualvm-plugin',
    'ui-path',
    'ui-archive-utility',
    'stateful-datatable-demo',
    'smart-workflow',
    'rule-engine-demos',
    'process-inspector',
    'process-analyzer',
    'persistence-utils',
    'pattern-demos',
    'master-detail-view',
    'mailstore',
    'ivy-load-test',
    'idp-connector',
    'html-dialog-utils',
    'html-dialog-demo',
    'gdpr',
    'express-importer',
    'error-handling-demos',
    'email-encryption',
    'docfactory',
    'dmn-decision-table',
    'db-utils',
    'connectivity-feature',
    'coffee-machine',
    'cms-live-editor',
    'case-process-viewer',
    'case-mail-component',
    'captcha-utils',
    'axonivy-words',
    'axonivy-rpa',
    'axonivy-portal',
    'axonivy-pdf',
    'axonivy-form-editor-demos',
    'axonivy-express',
    'axonivy-cells',
    'aspose-email-demo',
    'aspose-barcode',
    'approval-decision',
    'anonymous-demo',
    'ai-assistant'
);