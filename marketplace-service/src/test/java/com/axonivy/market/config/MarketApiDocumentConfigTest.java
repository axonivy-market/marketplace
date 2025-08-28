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
  void testBuildMarketCustomHeader_ShouldReturnGroupedOpenApiWithCorrectConfiguration() {
    GroupedOpenApi result = marketApiDocumentConfig.buildMarketCustomHeader();

    assertNotNull(result, "");
    assertEquals("api", result.getGroup());
    assertNotNull(result.getPathsToMatch());
    assertTrue(result.getPathsToMatch().contains("/api/**"));
    assertNotNull(result.getOpenApiCustomizers());
    assertFalse(result.getOpenApiCustomizers().isEmpty());
  }

  @Test
  void testCustomMarketHeaders_ShouldAddHeaderParametersToAllPaths() {
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
  void testAddHeaderParameters_ShouldAddParametersToAllNonNullOperations() {
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

    assertEquals(1, putOperation.getParameters().size());
    assertEquals(1, postOperation.getParameters().size());
    assertEquals(1, patchOperation.getParameters().size());
    assertEquals(1, deleteOperation.getParameters().size());

    verifyParameterDetails(putOperation.getParameters().get(0));
    verifyParameterDetails(postOperation.getParameters().get(0));
    verifyParameterDetails(patchOperation.getParameters().get(0));
    verifyParameterDetails(deleteOperation.getParameters().get(0));
  }

  @Test
  void testAddHeaderParameters_ShouldSkipNullOperations() {
    Operation postOperation = new Operation();
    pathItem.setPost(postOperation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    assertEquals(1, postOperation.getParameters().size());
    verifyParameterDetails(postOperation.getParameters().get(0));
  }

  @Test
  void testAddHeaderParameters_ShouldHandlePathItemWithNoOperations() {
    PathItem emptyPathItem = new PathItem();

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(emptyPathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    assertDoesNotThrow(() ->
        groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI)
    );
  }

  @Test
  void testCreateRequestedByHeader_ShouldCreateParameterWithCorrectProperties() {
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
  void testCustomMarketHeaders_ShouldHandleEmptyPaths() {
    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(List.of());

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();
    assertDoesNotThrow(() ->
        groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI)
    );
  }

  @Test
  void testAddHeaderParameters_ShouldNotDuplicateParametersOnMultipleCalls() {
    Operation postOperation = new Operation();
    pathItem.setPost(postOperation);

    when(openAPI.getPaths()).thenReturn(paths);
    when(paths.values()).thenReturn(Arrays.asList(pathItem));

    GroupedOpenApi groupedOpenApi = marketApiDocumentConfig.buildMarketCustomHeader();

    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);
    groupedOpenApi.getOpenApiCustomizers().get(0).customise(openAPI);

    assertEquals(2, postOperation.getParameters().size());
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
      assertEquals(1, pathItem.getPut().getParameters().size());
    }
    if (pathItem.getPost() != null) {
      assertEquals(1, pathItem.getPost().getParameters().size());
    }
    if (pathItem.getPatch() != null) {
      assertEquals(1, pathItem.getPatch().getParameters().size());
    }
    if (pathItem.getDelete() != null) {
      assertEquals(1, pathItem.getDelete().getParameters().size());
    }
  }

  private void verifyParameterDetails(Parameter parameter) {
    assertNotNull(parameter);
    assertEquals("header", parameter.getIn());
    assertEquals(REQUESTED_BY, parameter.getName());
    assertEquals("ivy", parameter.getDescription());
    assertTrue(parameter.getRequired());
    assertNotNull(parameter.getSchema());
    assertInstanceOf(StringSchema.class, parameter.getSchema());
  }
}