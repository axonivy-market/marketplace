package com.axonivy.market.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.BaseSetup.DEFAULT_HOST;
import static com.axonivy.market.BaseSetup.OPEN_API_SPEC_PATH;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApisUtilsTest {

    @LocalServerPort
    private int port;

    private String getSpecUrl() {
        return DEFAULT_HOST + port + "/api-docs";
    }

    @Test
    void testFetchAndValidateOpenApi() throws IOException {
        String savedPath = OpenApisUtils.fetchOpenApiYaml(getSpecUrl(), OPEN_API_SPEC_PATH);
        boolean isValid = OpenApisUtils.validateUsingSwaggerIO(savedPath);
        FileUtils.clearDirectory(Path.of(savedPath));
        assertTrue(isValid, "Expected remote Swagger validator to pass");
    }

    @Test
    void testFetchFailed() {
        String savedPath = OpenApisUtils.fetchOpenApiYaml("", "");
        assertEquals(StringUtils.EMPTY, savedPath, "Expected yml path to be EMPTY");
    }

    @Test
    void testValidateOpenApiFailed() {
        boolean isValid = OpenApisUtils.validateUsingSwaggerIO("");
        assertFalse(isValid, "Expected remote Swagger validator to fail");
    }

}
