INSERT INTO product (id, listed, released_versions, tags, type)
VALUES
    ('listed-product', true, '1.0.0', 'alpha,beta', 'connector'),
    ('documented-product', true, '2.0.0', 'gamma', 'connector'),
    ('hidden-product', false, '3.0.0', 'hidden', 'connector');

INSERT INTO artifact (id, artifact_id, doc, group_id, is_dependency, is_invalid_artifact, name, repo_url, type)
VALUES
    ('listed-artifact', 'listed-artifact', true, 'com.example', true, false, 'Listed Artifact',
        'https://example.com/listed-artifact', 'iar'),
    ('documented-artifact-1', 'documented-artifact-1', true, 'com.example', true, false,
        'Documented Artifact 1', 'https://example.com/documented-artifact-1', 'iar'),
    ('documented-artifact-2', 'documented-artifact-2', true, 'com.example', true, false,
        'Documented Artifact 2', 'https://example.com/documented-artifact-2', 'iar'),
    ('hidden-artifact', 'hidden-artifact', false, 'com.example', true, false, 'Hidden Artifact',
        'https://example.com/hidden-artifact', 'iar');

INSERT INTO product_artifacts (product_id, artifacts_id)
VALUES
    ('listed-product', 'listed-artifact'),
    ('documented-product', 'documented-artifact-1'),
    ('documented-product', 'documented-artifact-2'),
    ('hidden-product', 'hidden-artifact');

INSERT INTO product_name (product_id, language, name)
VALUES
    ('listed-product', 'en', 'Listed Product'),
    ('documented-product', 'en', 'Documented Product'),
    ('hidden-product', 'en', 'Hidden Product');

INSERT INTO product_description (product_id, language, short_description)
VALUES
    ('listed-product', 'en', 'Listed product short description'),
    ('documented-product', 'en', 'Documented product short description'),
    ('hidden-product', 'en', 'Hidden product short description');

INSERT INTO product_module_content (id, product_id, version, is_dependency, name, group_id, artifact_id, type)
VALUES ('listed-product-1.0.0-content', 'listed-product', '1.0.0', false, 'Listed Product', 'com.example',
        'listed-product', 'iar');

INSERT INTO product_module_content_description (product_module_content_id, language, description)
VALUES ('listed-product-1.0.0-content', 'en', 'Listed product description');

INSERT INTO product_module_content_setup (product_module_content_id, language, setup)
VALUES ('listed-product-1.0.0-content', 'en', 'Listed product setup');

INSERT INTO product_module_content_demo (product_module_content_id, language, demo)
VALUES ('listed-product-1.0.0-content', 'en', 'Listed product demo');

INSERT INTO product_module_content_component (product_module_content_id, language, component)
VALUES ('listed-product-1.0.0-content', 'en', 'Listed product component');

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
