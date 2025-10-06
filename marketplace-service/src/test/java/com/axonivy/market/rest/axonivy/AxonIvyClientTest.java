package com.axonivy.market.rest.axonivy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class AxonIvyClientTest {

    @SpyBean
    private AxonIvyClient axonIvyClient;

    @Test
    void testGetDocumentVersions() {
        List<String> versions = axonIvyClient.getDocumentVersions();
        assertFalse(versions.isEmpty(), "Expected to fetch at least one document");
    }

}
