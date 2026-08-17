INSERT INTO product (id, listed, released_versions, tags, type, market_directory)
VALUES
    ('case-process-viewer-utils', true, '13.2.3,12.0.10,12.0.11,12.0.12,13.2.0,13.2.2',
        'utils', 'utils', 'market/utils/case-process-viewer-utils/'),
    ('express-importer', true, '10.0.2,10.0.3,10.0.4,11.1.0,11.1.1,11.3.0,12.0.0,12.0.1',
        'utils,no-code', 'utils', 'market/utils/express-importer/'),
    ('portal', false,
        '11.4.0-m259,11.4.0-m260,11.4.0-m261,11.4.0-m262,11.4.0-m263,14.0.0-m286,14.0.0-m288,14.0.0-m289,14.0.0-m290,14.0.0-m291,14.0.0-m292,14.0.0-m293,14.0.0-m294,14.0.0-SNAPSHOT,8.0.1,8.0.3,8.0.4,8.0.5,8.0.6,8.0.7,8.0.8,8.0.9,8.0.10,8.0.11,8.0.12,8.0.13,8.0.14,8.0.15,8.0.16,8.0.17.1,8.0.18.1,8.0.19.1,8.0.20,8.0.24,8.0.25.1,8.0.26,8.0.27,8.0.28,8.0.30,8.0.32,8.0.34,8.0.34.1,8.0.35,8.0.36,8.0.37,8.0.38,8.0.39,9.1.0.0-SNAPSHOT,9.1.0,9.2.0,9.2.1,9.2.2,9.3.0.3,9.3.3,9.3.5,9.4.0-m224,9.4.0-m228,9.4.0-m229,9.4.0,10.0.0-m231,10.0.0,10.0.0.1,10.0.1,10.0.1.1,10.0.2,10.0.3,10.0.3.1,10.0.4,10.0.6,10.0.7,10.0.10,10.0.11,10.0.12,10.0.13.1,10.0.13.2,10.0.14,10.0.15,10.0.15.1,10.0.15.2,10.0.16,10.0.17,10.0.17.1,10.0.19,10.0.21,10.0.22,10.0.23,10.0.24,10.0.24.1,10.0.24.2,10.0.25,10.0.26,10.0.26.1,10.0.26.2,10.0.27,10.0.28,10.0.29,10.0.30,10.0.31,10.0.33,10.0.34,10.0.35,10.0.36,10.0.36.1,10.0.37,11.1.0-m233,11.1.0-m234,11.1.0-m235,11.1.0-m236,11.1.0-m237,11.1.0-m239,11.1.0-m240,11.1.0,11.2.0-m241,11.2.0-m242,11.2.0-m243,11.2.0-m243.1,11.2.0-m244,11.2.0-m245,11.2.0-m246.1,11.2.0-m247,11.2.0-m248,11.2.0-m249,11.2.0-m249.1,11.2.0,11.2.0.1,11.2.1,11.3.0-m251,11.3.0-m252,11.3.0-m253,11.3.0-m255,11.3.0-m256,11.3.0-m257,11.3.0-m257.1,11.3.0.2,11.3.1,12.0.0-m264,12.0.0-m265,12.0.0-m266,12.0.0,12.0.1,12.0.2,12.0.3,12.0.4,12.0.4.1,12.0.5,12.0.6,12.0.7,12.0.8,12.0.9,12.0.11,12.0.12,12.0.12.1,12.0.13,12.0.14,12.0.14.1,12.0.15,12.0.16,13.1.0-m269,13.1.0-m270,13.1.0-m271,13.1.0-m272,13.1.0-m273,13.1.0-m274,13.1.0-m275,13.1.0-m276,13.1.0,13.1.0.1,13.1.1,13.2.0-m277,13.2.0-m278,13.2.0-m280,13.2.0-m281,13.2.0-m282,13.2.0-m283,13.2.0-m284,13.2.0,13.2.0.1,13.2.0.3,13.2.0.4',
        'workflow-ui,utils', 'utils', 'market/utils/portal/');

INSERT INTO artifact (id, artifact_id, doc, group_id, is_dependency, is_invalid_artifact, name, repo_url, type)
VALUES
    ('1787ca03-9eab-4e8e-ace8-376fbf18b1fd', 'case-process-viewer-utils-product', NULL,
        'com.axonivy.utils.caseprocessviewer', NULL, false, 'Case Process Viewer Utils Product',
        'https://maven.axonivy.com', 'zip'),
    ('3659d15f-8e83-490f-86c6-d45b696e69ee', 'express-importer-product', NULL,
        'com.axonivy.portal.express', NULL, false, 'Express Importer Product',
        'https://maven.axonivy.com', 'zip'),
    ('b4543172-fe0e-4200-aebf-abe8a45cc2bc', 'express-importer', NULL,
        'com.axonivy.portal.express', NULL, false, 'Express Importer',
        'https://maven.axonivy.com', 'jar'),
    ('41f3932e-7cf2-487a-ba20-05ad3f092453', 'portal-guide', true,
        'com.axonivy.portal', NULL, false, 'Portal Guide',
        'https://nexus-mirror.axonivy.com/repository/maven', 'zip'),
    ('5ee11eb5-0244-4e3f-a4ea-34367bbd93eb', 'portal-components', NULL,
        'com.axonivy.portal', NULL, false, 'Portal Component',
        NULL, NULL),
    ('e58e6bea-a2c0-415c-beed-9dcd87b0bce5', 'portal-product', NULL,
        'com.axonivy.portal', NULL, false, 'Portal Product',
        NULL, 'zip');

INSERT INTO product_artifacts (product_id, artifacts_id)
VALUES
    ('case-process-viewer-utils', '1787ca03-9eab-4e8e-ace8-376fbf18b1fd'),
    ('express-importer', '3659d15f-8e83-490f-86c6-d45b696e69ee'),
    ('express-importer', 'b4543172-fe0e-4200-aebf-abe8a45cc2bc'),
    ('portal', '41f3932e-7cf2-487a-ba20-05ad3f092453'),
    ('portal', '5ee11eb5-0244-4e3f-a4ea-34367bbd93eb'),
    ('portal', 'e58e6bea-a2c0-415c-beed-9dcd87b0bce5');

INSERT INTO product_name (product_id, language, name)
VALUES
    ('case-process-viewer-utils', 'en', 'Case Process Viewer'),
    ('express-importer', 'en', 'Express Importer'),
    ('portal', 'en', 'Axon Ivy Portal');

INSERT INTO product_description (product_id, language, short_description)
VALUES
    ('case-process-viewer-utils', 'en',
        'This Axon Ivy utility visualizes the current progress of a running process by highlighting the active task as well as all completed tasks directly within the process diagram.'),
    ('express-importer', 'en', 'Integrates your No-Code initiatives into your Designer project.'),
    ('portal', 'en', 'The Axon Ivy Portal is the single point of contact for any end-user using the Axon Ivy platform.');

INSERT INTO product_module_content (id, product_id, version, is_dependency, name, group_id, artifact_id, type)
VALUES ('case-process-viewer-utils-13.2.3', 'case-process-viewer-utils', '13.2.3', false,
        'Case Process Viewer Utils', 'com.axonivy.utils.caseprocessviewer',
        'case-process-viewer-utils', 'iar');

INSERT INTO product_module_content_description (product_module_content_id, language, description)
VALUES ('case-process-viewer-utils-13.2.3', 'en',
        'This Axon Ivy component visually represents the process flow of your current case. It highlights both the active task and all completed tasks directly on the process diagram.');

INSERT INTO product_module_content_setup (product_module_content_id, language, setup)
VALUES ('case-process-viewer-utils-13.2.3', 'en', 'Add the Component to Your JSF Page');

INSERT INTO product_module_content_demo (product_module_content_id, language, demo)
VALUES ('case-process-viewer-utils-13.2.3', 'en', '1. Start **Purchase Request Demo** process');

INSERT INTO product_module_content_component (product_module_content_id, language, component)
VALUES ('case-process-viewer-utils-13.2.3', 'en', '');

INSERT INTO product_marketplace_data (id, installation_count, synchronized_installation_count, custom_order)
VALUES
    ('case-process-viewer-utils', 0, false, 2),
    ('express-importer', 196, true, 3),
    ('portal', 6536, true, 1);

INSERT INTO product_designer_installation (id, product_id, designer_version, installation_count)
VALUES ('express-importer-10.0.22', 'express-importer', '10.0.22', 2);

INSERT INTO product_security_info (repo_name, branch_protection_enabled, last_commit_date,
                                  number_of_secret_scanning_alerts, secret_scanning_status,
                                  code_scanning_alerts, code_scanning_status, dependabot_alerts,
                                  dependabot_status, visibility)
VALUES
    ('alpha-security', true, TIMESTAMP '2026-08-01 10:00:00', 5, 'DISABLED',
        '{"critical":0,"high":0,"medium":1,"low":2}', 'ENABLED',
        '{"critical":0,"high":1,"medium":0,"low":0}', 'NO_PERMISSION', 'public'),
    ('portal-connector', false, TIMESTAMP '2026-08-10 10:00:00', 1, 'ENABLED',
        '{"critical":1,"high":0,"medium":0,"low":0}', 'DISABLED',
        '{"critical":0,"high":0,"medium":1,"low":1}', 'NOT_SUPPORTED', 'public'),
    ('zeta-security', false, TIMESTAMP '2026-08-05 10:00:00', 0, 'NOT_SUPPORTED',
        '{"critical":0,"high":0,"medium":0,"low":0}', 'NO_PERMISSION',
        '{"critical":0,"high":0,"medium":0,"low":2}', 'DISABLED', 'private');

INSERT INTO github_repo (id, name, product_id, html_url, focused)
VALUES
    ('repo-portal', 'portal', 'portal', 'https://github.com/axonivy-market/portal', true),
    ('repo-connectivity', 'connectivity-demo', 'connectivity-demo',
        'https://github.com/axonivy-market/connectivity-demo', true),
    ('repo-microsoft-365', 'microsoft-365', 'microsoft-365',
        'https://github.com/axonivy-market/microsoft-365', true),
    ('repo-jira-connector', 'jira-connector', 'jira-connector',
        'https://github.com/axonivy-market/jira-connector', null),
    ('repo-slack-connector', 'slack-connector', 'slack-connector',
        'https://github.com/axonivy-market/slack-connector', null),
    ('repo-archived-demo', 'archived-demo', 'archived-demo',
        'https://github.com/axonivy-market/archived-demo', false);

INSERT INTO workflow_information (id, repository_id, workflow_type, last_built, conclusion, last_built_run_url,
                                  current_workflow_state)
VALUES
    -- portal: the newest CI run succeeded, the older one failed
    ('wf-portal-ci-old', 'repo-portal', 'CI', TIMESTAMP '2026-06-01 10:00:00', 'failure',
        'https://github.com/axonivy-market/portal/actions/runs/1', 'active'),
    ('wf-portal-ci-new', 'repo-portal', 'CI', TIMESTAMP '2026-07-01 10:00:00', 'success',
        'https://github.com/axonivy-market/portal/actions/runs/2', 'active'),
    -- connectivity-demo: the newest CI run failed, the older one succeeded
    ('wf-connectivity-ci-old', 'repo-connectivity', 'CI', TIMESTAMP '2026-06-15 10:00:00', 'success',
        'https://github.com/axonivy-market/connectivity-demo/actions/runs/3', 'active'),
    ('wf-connectivity-ci-new', 'repo-connectivity', 'CI', TIMESTAMP '2026-07-15 10:00:00', 'failure',
        'https://github.com/axonivy-market/connectivity-demo/actions/runs/4', 'active'),
    -- microsoft-365 has no CI run at all, only a DEV one
    ('wf-microsoft-365-dev', 'repo-microsoft-365', 'DEV', TIMESTAMP '2026-07-20 10:00:00', 'success',
        'https://github.com/axonivy-market/microsoft-365/actions/runs/5', 'active'),
    ('wf-jira-connector-ci', 'repo-jira-connector', 'CI', TIMESTAMP '2026-07-02 10:00:00', 'failure',
        'https://github.com/axonivy-market/jira-connector/actions/runs/6', 'active'),
    ('wf-slack-connector-ci', 'repo-slack-connector', 'CI', TIMESTAMP '2026-07-03 10:00:00', 'success',
        'https://github.com/axonivy-market/slack-connector/actions/runs/7', 'active');
