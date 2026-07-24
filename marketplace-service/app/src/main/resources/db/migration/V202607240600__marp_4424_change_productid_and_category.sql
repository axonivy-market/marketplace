
--smart-workflow
UPDATE product SET market_directory = 'market/utils/smart-workflow-utils/' WHERE id = 'smart-workflow';

--IDP Connector
UPDATE github_repo SET name = 'idp-utils', html_url = 'https://github.com/axonivy-market/idp-utils', product_id =
'idp-utils' WHERE id = 'idp-connector';
UPDATE image SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_designer_installation SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_marketplace_data SET id = 'idp-utils' WHERE id = 'idp-connector';
UPDATE product_json_content SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_module_content SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_security_info SET repo_name = 'idp-utils' WHERE repo_name = 'idp-connector';
UPDATE feedback SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_dependency SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'idp-connector';

UPDATE temp_row SET market_directory = 'market/utils/idp-utils/', repository_name = 'axonivy-market/idp-utils', id =
'idp-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_name SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';
UPDATE product_artifacts SET product_id = 'idp-utils' WHERE product_id = 'idp-connector';

DELETE FROM product WHERE id = 'idp-connector';

COMMIT;


--Anonymous Demos
UPDATE product SET market_directory = 'market/demo/anonymous-demos/' WHERE id = 'anonymous-demos';

--Aspose.BarCode
UPDATE product SET market_directory = 'market/utils/aspose-barcode/' WHERE id = 'aspose-barcode-demo';

--Aspose.Email Demo
UPDATE product SET market_directory = 'market/utils/aspose-email/' WHERE id = 'aspose-email-demo';

--Axon Ivy Form Editor Demos
UPDATE product SET market_directory = 'market/demo/form-editor/' WHERE id = 'form-editor-demo';

--Axon Ivy RPA
UPDATE github_repo SET name = 'metaproc-utils', html_url = 'https://github.com/axonivy-market/metaproc-utils',
product_id = 'metaproc-utils' WHERE id = 'metaproc-connector';
UPDATE image SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_designer_installation SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_marketplace_data SET id = 'metaproc-utils' WHERE id = 'metaproc-connector';
UPDATE product_json_content SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_module_content SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_security_info SET repo_name = 'metaproc-utils' WHERE repo_name = 'metaproc-connector';
UPDATE feedback SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_dependency SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'metaproc-connector';

UPDATE temp_row SET market_directory = 'market/utils/metaproc-utils/', repository_name = 'axonivy-market/metaproc-utils',
id = 'metaproc-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_name SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';
UPDATE product_artifacts SET product_id = 'metaproc-utils' WHERE product_id = 'metaproc-connector';

DELETE FROM product WHERE id = 'metaproc-connector';

COMMIT;


--Case Mail Component
UPDATE github_repo SET name = 'case-mail-component-utils',
html_url = 'https://github.com/axonivy-market/case-mail-component-utils', product_id = 'case-mail-component-utils'
WHERE id = 'case-mail-component-connector';
UPDATE image SET product_id = 'case-mail-component-utils' WHERE product_id = 'case-mail-component-connector';
UPDATE product_designer_installation SET product_id = 'case-mail-component-utils'
WHERE product_id = 'case-mail-component-connector';
UPDATE product_marketplace_data SET id = 'case-mail-component-utils' WHERE id = 'case-mail-component-connector';
UPDATE product_json_content SET product_id = 'case-mail-component-utils'
WHERE product_id = 'case-mail-component-connector';
UPDATE product_module_content SET product_id = 'case-mail-component-utils'
WHERE product_id = 'case-mail-component-connector';
UPDATE product_security_info SET repo_name = 'case-mail-component-utils'
WHERE repo_name = 'case-mail-component-connector';
UPDATE feedback SET product_id = 'case-mail-component-utils' WHERE product_id = 'case-mail-component-connector';
UPDATE product_dependency SET product_id = 'case-mail-component-utils'
WHERE product_id = 'case-mail-component-connector';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'case-mail-component-connector';

UPDATE temp_row SET market_directory = 'market/utils/case-mail-component-utils/',
repository_name = 'axonivy-market/case-mail-component-utils', id = 'case-mail-component-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'case-mail-component-utils' WHERE product_id = 'case-mail-component-connector';
UPDATE product_name SET product_id = 'case-mail-component-utils' WHERE product_id = 'case-mail-component-connector';
UPDATE product_artifacts SET product_id = 'case-mail-component-utils' WHERE product_id = 'case-mail-component-connector';

DELETE FROM product WHERE id = 'case-mail-component-connector';

COMMIT;


--Case Process Viewer
UPDATE github_repo SET name = 'case-process-viewer-utils',
html_url = 'https://github.com/axonivy-market/case-process-viewer-utils', product_id = 'case-process-viewer-utils'
WHERE id = 'case-process-viewer';
UPDATE image SET product_id = 'case-process-viewer-utils' WHERE product_id = 'case-process-viewer';
UPDATE product_designer_installation SET product_id = 'case-process-viewer-utils'
WHERE product_id = 'case-process-viewer';
UPDATE product_marketplace_data SET id = 'case-process-viewer-utils' WHERE id = 'case-process-viewer';
UPDATE product_json_content SET product_id = 'case-process-viewer-utils'
WHERE product_id = 'case-process-viewer';
UPDATE product_module_content SET product_id = 'case-process-viewer-utils'
WHERE product_id = 'case-process-viewer';
UPDATE product_security_info SET repo_name = 'case-process-viewer-utils'
WHERE repo_name = 'case-process-viewer';
UPDATE feedback SET product_id = 'case-process-viewer-utils' WHERE product_id = 'case-process-viewer';
UPDATE product_dependency SET product_id = 'case-process-viewer-utils'
WHERE product_id = 'case-process-viewer';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'case-process-viewer';

UPDATE temp_row SET market_directory = 'market/utils/case-process-viewer-utils/',
repository_name = 'axonivy-market/case-process-viewer-utils', id = 'case-process-viewer-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'case-process-viewer-utils' WHERE product_id = 'case-process-viewer';
UPDATE product_name SET product_id = 'case-process-viewer-utils' WHERE product_id = 'case-process-viewer';
UPDATE product_artifacts SET product_id = 'case-process-viewer-utils' WHERE product_id = 'case-process-viewer';

DELETE FROM product WHERE id = 'case-process-viewer';

COMMIT;


--Chat GPT Assistant
UPDATE product SET market_directory = 'market/connector/openai-assistant/' WHERE id = 'openai-assistant';

--Connectivity Feature
UPDATE product SET market_directory = 'market/demo/connectivity/' WHERE id = 'connectivity-demo';

--Custom Mail Feature
UPDATE product SET market_directory = 'market/utils/custom-mail/' WHERE id = 'custom-mail-demo';

--Database CRUD
UPDATE product SET market_directory = 'market/utils/db-demo/' WHERE id = 'db-demos';

--Error Handling Demos
UPDATE product SET market_directory = 'market/demo/error-handling/' WHERE id = 'error-handling-demo';

--GraphGL Demo
UPDATE github_repo SET name = 'graphql-connector',
html_url = 'https://github.com/axonivy-market/graphql-connector', product_id = 'graphql-connector'
WHERE id = 'graphql-demo';
UPDATE image SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_designer_installation SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_marketplace_data SET id = 'graphql-connector' WHERE id = 'graphql-demo';
UPDATE product_json_content SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_module_content SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_security_info SET repo_name = 'graphql-connector' WHERE repo_name = 'graphql-demo';
UPDATE feedback SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_dependency SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'graphql-demo';

UPDATE temp_row SET market_directory = 'market/connector/graphql-connector/',
repository_name = 'axonivy-market/graphql-connector', id = 'graphql-connector';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_name SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';
UPDATE product_artifacts SET product_id = 'graphql-connector' WHERE product_id = 'graphql-demo';

DELETE FROM product WHERE id = 'graphql-demo';

COMMIT;


--HTML Dialog Demos
UPDATE product SET market_directory = 'market/demo/html-dialog/' WHERE id = 'html-dialog-demo';

--Mailstore
UPDATE github_repo SET name = 'mailstore-utils',
html_url = 'https://github.com/axonivy-market/mailstore-utils', product_id = 'mailstore-utils'
WHERE id = 'mailstore-connector';
UPDATE image SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_designer_installation SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_marketplace_data SET id = 'mailstore-utils' WHERE id = 'mailstore-connector';
UPDATE product_json_content SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_module_content SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_security_info SET repo_name = 'mailstore-utils' WHERE repo_name = 'mailstore-connector';
UPDATE feedback SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_dependency SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'mailstore-connector';

UPDATE temp_row SET market_directory = 'market/utils/mailstore-utils/',
repository_name = 'axonivy-market/mailstore-utils', id = 'mailstore-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_name SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';
UPDATE product_artifacts SET product_id = 'mailstore-utils' WHERE product_id = 'mailstore-connector';

DELETE FROM product WHERE id = 'mailstore-connector';

COMMIT;


--Master-Detail View
UPDATE github_repo SET name = 'master-detail-utils',
html_url = 'https://github.com/axonivy-market/master-detail-utils', product_id = 'master-detail-utils'
WHERE id = 'master-detail-demo';
UPDATE image SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_designer_installation SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_marketplace_data SET id = 'master-detail-utils' WHERE id = 'master-detail-demo';
UPDATE product_json_content SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_module_content SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_security_info SET repo_name = 'master-detail-utils' WHERE repo_name = 'master-detail-demo';
UPDATE feedback SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_dependency SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';

BEGIN;

CREATE TEMP TABLE temp_row ON COMMIT DROP AS
SELECT * FROM product WHERE id = 'master-detail-demo';

UPDATE temp_row SET market_directory = 'market/utils/master-detail-utils/',
repository_name = 'axonivy-market/master-detail-utils', id = 'master-detail-utils';

INSERT INTO product SELECT * FROM temp_row;

UPDATE product_description SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_name SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';
UPDATE product_artifacts SET product_id = 'master-detail-utils' WHERE product_id = 'master-detail-demo';

DELETE FROM product WHERE id = 'master-detail-demo';

COMMIT;


--Pattern Demos
UPDATE product SET market_directory = 'market/demo/pattern-demos/' WHERE id = 'pattern-demos';

--Rule Engine Demos
UPDATE product SET market_directory = 'market/demo/rule-engine/' WHERE id = 'rule-engine-demo';

--Stateful Datatable Demo
UPDATE product SET market_directory = 'market/demo/stateful-datatable/' WHERE id = 'stateful-datatable-demo';

--Workflow Demos
UPDATE product SET market_directory = 'market/demo/workflow/' WHERE id = 'workflow-demo';

--Ivy load test
UPDATE product SET market_directory = 'market/utils/ivy-load-test/' WHERE id = 'ivy-load-test';
