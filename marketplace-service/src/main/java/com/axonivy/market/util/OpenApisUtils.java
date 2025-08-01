package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenApisUtils {

    private static final String VALIDATOR_URL = "https://validator.swagger.io/validator/debug";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static String fetchOpenApiYaml(String specUrl, String outputPath) throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(specUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Files.createDirectories(new File(outputPath).getParentFile().toPath());
            Files.write(new File(outputPath).toPath(), Objects.requireNonNull(response.getBody()).getBytes());
            return outputPath;
        } else {
            throw new IOException("Failed to fetch OpenAPI spec. HTTP " + response.getStatusCode());
        }
    }

    public static boolean validateUsingSwaggerIO(String specFilePath) {
        try {
            String spec = String.join(CommonConstants.NEW_LINE, Files.readAllLines(new File(specFilePath).toPath()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/yaml"));

            HttpEntity<String> request = new HttpEntity<>(spec, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    VALIDATOR_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }

            String body = response.getBody();
            return body == null || (!body.contains("messages") && !body.contains("error"));
        } catch (Exception e) {
            return false;
        }
    }
}
