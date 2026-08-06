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
