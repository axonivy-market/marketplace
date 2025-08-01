package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenApisUtils {

    private static final String VALIDATOR_URL = "https://validator.swagger.io/validator/debug";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static String fetchOpenApiYaml(String specUrl, String outputPath) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(specUrl, String.class);
            Files.createDirectories(new File(outputPath).getParentFile().toPath());
            Files.writeString(new File(outputPath).toPath(), Objects.requireNonNull(response.getBody()),
                    StandardCharsets.UTF_8);
            return outputPath;
        } catch (RestClientException | IOException | IllegalArgumentException e) {
            log.warn("fetchOpenApiYaml Failed to fetch spec file", e);
            return StringUtils.EMPTY;
        }
    }

    public static boolean validateUsingSwaggerIO(String specFilePath) {
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
            log.warn("validateUsingSwaggerIO Failed to validate spec file", e);
            return false;
        }
    }
}
