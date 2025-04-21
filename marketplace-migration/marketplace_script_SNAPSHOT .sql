UPDATE metadata m
SET url = REPLACE(url, 'https://maven.axonivy.com', 'https://nexus-mirror.axonivy.com/repository/maven')
WHERE m.url LIKE 'https://maven.axonivy.com%'