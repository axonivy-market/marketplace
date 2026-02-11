package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.axonivy.market.BaseSetup.DEFAULT_HOST;
import static com.axonivy.market.BaseSetup.OPEN_API_SPEC_PATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApisUtilsTest {

    @LocalServerPort
    private int port;
    private static final String VALIDATOR_URL = "https://validator.swagger.io/validator/debug";
    private static final RestTemplate restTemplate = new RestTemplate();

    private String getSpecUrl() {
        return DEFAULT_HOST + port + "/api-docs";
    }

    @Test
    void testFetchAndValidateOpenApi() throws IOException {
        String savedPath = fetchOpenApiYaml();
        boolean isValid = validateUsingSwaggerIO(savedPath);
        FileUtils.clearDirectory(Path.of(savedPath));
        assertTrue(isValid, "Expected remote Swagger validator to pass");
    }


    private String fetchOpenApiYaml() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(getSpecUrl(), String.class);
            Files.createDirectories(new File(OPEN_API_SPEC_PATH).getParentFile().toPath());
            Files.writeString(new File(OPEN_API_SPEC_PATH).toPath(), Objects.requireNonNull(response.getBody()),
                    StandardCharsets.UTF_8);
            return OPEN_API_SPEC_PATH;
        } catch (RestClientException | IOException | IllegalArgumentException e) {
            return StringUtils.EMPTY;
        }
    }

    private boolean validateUsingSwaggerIO(String specFilePath) {
        try {
            var spec = String.join(CommonConstants.NEW_LINE, Files.readAllLines(new File(specFilePath).toPath()));
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/yaml"));
            HttpEntity<String> request = new HttpEntity<>(spec, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    VALIDATOR_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                return body == null || (!body.contains("messages") && !body.contains("error"));
            }
            return false;
        } catch (IOException | RestClientException e) {
            return false;
        }
    }

}
