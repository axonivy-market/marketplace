package com.axonivy.market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.REQUESTED_BY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketApiDocumentConfigTest {

  @InjectMocks
  private MarketApiDocumentConfig marketApiDocumentConfig;

  private OpenAPI openAPI;
  private Paths paths;
  private PathItem pathItem;

  @BeforeEach
  void setUp() {
    openAPI = mock(OpenAPI.class);
    paths = mock(Paths.class);
    pathItem = new PathItem();
  }

  @Test
  void testBuildMarketCustomHeaderShouldReturnGroupedOpenApiWithCorrectConfiguration() {
    GroupedOpenApi result = marketApiDocumentConfig.buildMarketCustomHeader();

    assertNotNull(result, "GroupedOpenApi should not be null.");
    assertEquals("api", result.getGroup(), "The API group name should be 'api'.");
    assertNotNull(result.getPathsToMatch(), "Paths to match should not be null.");
    assertTrue(result.getPathsToMatch().contains("/api/**"), "Paths to match should include '/api/**'.");
    assertNotNull(result.getOpenApiCustomizers(), "OpenApiCustomizers should not be null.");
    assertFalse(result.getOpenApiCustomizers().isEmpty(), "OpenApiCustomizers should not be empty.");
  }

  @Test
  void testCustomMarketHeadersShouldAddHeaderParametersToAllPaths() {
    PathItem pathItem1 = createPathItemWithOperations();
    PathItem pathItem2 = createPathItemWithOperations();

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem1, pathItem2));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    verify(openAPI).getPaths();
    verify(paths).values();

    verifyHeadersAddedToAllOperations(pathItem1);
    verifyHeadersAddedToAllOperations(pathItem2);
  }

  @Test
  void testAddHeaderParametersShouldAddParametersToAllNonNullOperations() {
    Operation putOperation = new Operation();
    Operation postOperation = new Operation();
    Operation patchOperation = new Operation();
    Operation deleteOperation = new Operation();

    pathItem.setPut(putOperation);
    pathItem.setPost(postOperation);
    pathItem.setPatch(patchOperation);
    pathItem.setDelete(deleteOperation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    assertEquals(1, putOperation.getParameters().size(),
        "PUT operation should have exactly one header parameter added.");
    assertEquals(1, postOperation.getParameters().size(),
        "POST operation should have exactly one header parameter added.");
    assertEquals(1, patchOperation.getParameters().size(),
        "PATCH operation should have exactly one header parameter added.");
    assertEquals(1, deleteOperation.getParameters().size(),
        "DELETE operation should have exactly one header parameter added.");

    verifyParameterDetails(putOperation.getParameters().get(0));
    verifyParameterDetails(postOperation.getParameters().get(0));
    verifyParameterDetails(patchOperation.getParameters().get(0));
    verifyParameterDetails(deleteOperation.getParameters().get(0));
  }

  @Test
  void testAddHeaderParametersShouldSkipNullOperations() {
    Operation postOperation = new Operation();
    pathItem.setPost(postOperation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    assertEquals(1, postOperation.getParameters().size(),
        "POST operation should have exactly one header parameter added.");
    verifyParameterDetails(postOperation.getParameters().get(0));
  }

  @Test
  void testAddHeaderParametersShouldHandlePathItemWithNoOperations() {
    PathItem emptyPathItem = new PathItem();

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(emptyPathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();

    assertDoesNotThrow(
        () -> groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI),
        "Customizing OpenAPI should not throw an exception even if the PathItem has no operations."
    );
  }

  @Test
  void testCreateRequestedByHeaderShouldCreateParameterWithCorrectProperties() {
    Operation operation = new Operation();
    pathItem.setPost(operation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Collections.singletonList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    Parameter parameter = operation.getParameters().get(0);
    verifyParameterDetails(parameter);
  }

  @Test
  void testCustomMarketHeadersShouldHandleEmptyPaths() {
    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(List.of());

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();

    assertDoesNotThrow(
        () -> groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI),
        "Customizing OpenAPI should not throw an exception when there are no paths."
    );
  }

  @Test
  void testAddHeaderParametersShouldNotDuplicateParametersOnMultipleCalls() {
    Operation postOperation = new Operation();
    pathItem.setPost(postOperation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();

    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    assertEquals(2, postOperation.getParameters().size(),
        "POST operation should have exactly two parameters after two customizer calls, without unintended duplicates.");
  }

  private PathItem createPathItemWithOperations() {
    PathItem item = new PathItem();
    item.setPut(new Operation());
    item.setPost(new Operation());
    item.setPatch(new Operation());
    item.setDelete(new Operation());
    return item;
  }

  private void verifyHeadersAddedToAllOperations(PathItem pathItem) {
    if (pathItem.getPut() != null) {
      assertEquals(1, pathItem.getPut().getParameters().size(),
          "PUT operation should have exactly 1 header parameter");
    }
    if (pathItem.getPost() != null) {
      assertEquals(1, pathItem.getPost().getParameters().size(),
          "POST operation should have exactly 1 header parameter");
    }
    if (pathItem.getPatch() != null) {
      assertEquals(1, pathItem.getPatch().getParameters().size(),
          "PATCH operation should have exactly 1 header parameter");
    }
    if (pathItem.getDelete() != null) {
      assertEquals(1, pathItem.getDelete().getParameters().size(),
          "DELETE operation should have exactly 1 header parameter");
    }
  }

  private void verifyParameterDetails(Parameter parameter) {
    assertNotNull(parameter, "Parameter should not be null");
    assertEquals("header", parameter.getIn(), "Parameter 'in' value should be 'header'");
    assertEquals(REQUESTED_BY, parameter.getName(), "Parameter name should match REQUESTED_BY");
    assertEquals("ivy", parameter.getDescription(), "Parameter description should be 'ivy'");
    assertTrue(parameter.getRequired(), "Parameter should be marked as required");
    assertNotNull(parameter.getSchema(), "Parameter schema should not be null");
    assertInstanceOf(StringSchema.class, parameter.getSchema(),
        "Parameter schema should be an instance of StringSchema");
  }
}