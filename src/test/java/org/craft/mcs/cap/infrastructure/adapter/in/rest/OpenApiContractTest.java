package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.StreamSupport;
import org.craft.mcs.cap.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTest {

  @LocalServerPort
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void openApiDescribesJsonMediaTypesAndStrongDtoSchemas() throws Exception {
    final var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    );

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(contentType ->
            assertThat(MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue()
        );

    final var openApi = objectMapper.readTree(response.body());

    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/classification", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/north-american", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/chemical-profiles/sugar-acidity-zone/plot-metadata", "get");
    assertJsonResponse(openApi, "/feedstock-batches", "get");
    assertJsonResponse(openApi, "/feedstock-batches", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "post", "201");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/abv-prediction", "get");

    assertJsonRequest(openApi, "/feedstock-batches", "post");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements", "post");
    assertJsonRequest(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");

    assertStringFormat(schemaProperty(openApi, "AppleVarietyClassificationResponse", "varietyId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockMeasurementResponse", "measuredAt"), "date-time");
    assertStringFormat(schemaProperty(openApi, "FeedstockSeasonResponse", "startsOn"), "date");
    assertArray(schemaProperty(openApi, "AppleVarietySugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockMeasurementSugarAcidityZonePointBatchResponse", "unplottable"));

    assertEnumProperty(openApi, "CreateFeedstockBatchRequest", "type", "APPLE_JUICE");
    assertEnumProperty(openApi, "ChemicalProfileCapabilityAssessmentResponse", "capability", "SUGAR_ACIDITY_PLOT");
    assertEnumProperty(openApi, "PotentialAbvPredictionResponse", "confidence", "LOW");

    assertRequiredProperties(
        openApi,
        "CreateFeedstockBatchRequest",
        "ciderMakerId",
        "name",
        "type",
        "season",
        "availableVolumeLiters"
    );
    assertNotRequiredProperties(openApi, "CreateFeedstockBatchRequest", "source", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "note"));

    assertRequiredProperties(openApi, "CreateFeedstockMeasurementRequest", "name", "chemicalProfile", "measuredAt");
    assertNotRequiredProperties(openApi, "CreateFeedstockMeasurementRequest", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockMeasurementRequest", "note"));

    assertRequiredProperties(openApi, "FeedstockMeasurementResponse", "measurementId", "ciderMakerId", "batchId", "name");
    assertNotRequiredProperties(openApi, "FeedstockMeasurementResponse", "measuredAt", "chemicalProfile", "note");
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "measuredAt"));
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "chemicalProfile"));
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "note"));

    assertRequiredProperties(openApi, "ProfileTargetDistanceResponse", "status", "insideSugarAcidityTarget", "insideTarget");
    assertNotRequiredProperties(openApi, "ProfileTargetDistanceResponse", "value", "tannin", "nearestTargetTannin");
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "value"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "nearestTargetTannin"));

    assertRequiredProperties(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "ciderMakerId");
    assertNotRequiredProperties(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "batchId", "season", "after", "limit");
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "batchId"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "season"));

    assertParameterRequired(openApi, "/feedstock-batches", "get", "ciderMakerId", true);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonStartsOn", false);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonEndsOn", false);

    assertOneOfSchema(openApi, "ChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "StatisticalChemicalMeasurementRequest");
    assertOneOfSchema(openApi, "ChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "StatisticalChemicalMeasurementResponse");
    assertOneOfSchema(openApi, "ProfileTargetRequest");
    assertNoAllOfSchema(openApi, "PredefinedZoneProfileTargetRequest");
    assertNoAllOfSchema(openApi, "CustomRectangleProfileTargetRequest");
    assertOneOfSchema(openApi, "SugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "ExactSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "RangeSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "StatisticalSugarAcidityAxisPointResponse");
    assertNoAllOfSchema(openApi, "CalculatedSugarAcidityAxisPointResponse");
    assertOneOfSchema(openApi, "SugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "RectangleSugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "EllipseSugarAcidityGraphZoneResponse");
    assertNoSchemaComposesDiscriminatedUnionThroughAllOf(openApi);
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method
  ) {
    assertJsonResponse(openApi, path, method, "200");
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method,
      String status
  ) {
    final var content = operation(openApi, path, method)
        .path("responses")
        .path(status)
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s response content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard response content type", method, path)
        .isFalse();
  }

  private static void assertJsonRequest(
      JsonNode openApi,
      String path,
      String method
  ) {
    final var content = operation(openApi, path, method)
        .path("requestBody")
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s request content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard request content type", method, path)
        .isFalse();
  }

  private static JsonNode operation(JsonNode openApi, String path, String method) {
    final var operation = openApi.path("paths").path(path).path(method);
    assertThat(operation.isMissingNode())
        .as("%s %s operation exists", method, path)
        .isFalse();
    return operation;
  }

  private static JsonNode schemaProperty(JsonNode openApi, String schemaName, String propertyName) {
    final var property = openApi
        .path("components")
        .path("schemas")
        .path(schemaName)
        .path("properties")
        .path(propertyName);

    assertThat(property.isMissingNode())
        .as("%s.%s exists", schemaName, propertyName)
        .isFalse();
    return property;
  }

  private static void assertRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).contains(propertyNames);
  }

  private static void assertNotRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).doesNotContain(propertyNames);
  }

  private static java.util.List<String> requiredProperties(JsonNode openApi, String schemaName) {
    return StreamSupport.stream(openApi
            .path("components")
            .path("schemas")
            .path(schemaName)
            .path("required")
            .spliterator(), false)
        .map(JsonNode::asText)
        .toList();
  }

  private static void assertNullable(JsonNode property) {
    final var nullable = property.path("nullable").asBoolean(false)
        || hasType(property, "null")
        || StreamSupport.stream(property.path("anyOf").spliterator(), false)
        .anyMatch(schema -> hasType(schema, "null"));

    assertThat(nullable).isTrue();
  }

  private static void assertParameterRequired(
      JsonNode openApi,
      String path,
      String method,
      String parameterName,
      boolean required
  ) {
    final var parameter = StreamSupport.stream(operation(openApi, path, method)
            .path("parameters")
            .spliterator(), false)
        .filter(candidate -> parameterName.equals(candidate.path("name").asText()))
        .findFirst();

    assertThat(parameter)
        .as("%s %s parameter %s exists", method, path, parameterName)
        .isPresent();
    assertThat(parameter.orElseThrow().path("required").asBoolean(false))
        .as("%s %s parameter %s required", method, path, parameterName)
        .isEqualTo(required);
  }

  private static void assertOneOfSchema(JsonNode openApi, String schemaName) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.path("oneOf").isArray())
        .as("%s should remain a discriminated oneOf union", schemaName)
        .isTrue();
  }

  private static void assertNoAllOfSchema(JsonNode openApi, String schemaName) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.has("allOf"))
        .as("%s should be a flat object schema for TypeScript generators", schemaName)
        .isFalse();
    assertThat(schema.path("type").asText())
        .as("%s type", schemaName)
        .isEqualTo("object");
  }

  private static void assertNoSchemaComposesDiscriminatedUnionThroughAllOf(JsonNode openApi) {
    final var schemas = openApi.path("components").path("schemas");
    final var schemaProperties = schemas.properties();
    final var discriminatedUnionSchemaNames = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("oneOf").isArray())
        .map(java.util.Map.Entry::getKey)
        .toList();

    final var recursiveCompositionRefs = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("allOf").isArray())
        .flatMap(entry -> StreamSupport.stream(entry.getValue().path("allOf").spliterator(), false)
            .map(part -> part.path("$ref").asText())
            .filter(ref -> !ref.isBlank())
            .map(ref -> entry.getKey() + " -> " + ref.substring(ref.lastIndexOf('/') + 1)))
        .filter(ref -> discriminatedUnionSchemaNames.stream().anyMatch(ref::endsWith))
        .toList();

    assertThat(recursiveCompositionRefs)
        .as("schemas must not compose discriminated oneOf unions through allOf")
        .isEmpty();
  }

  private static void assertStringFormat(JsonNode property, String format) {
    assertThat(hasType(property, "string")).isTrue();
    assertThat(property.path("format").asText()).isEqualTo(format);
  }

  private static boolean hasType(JsonNode property, String type) {
    if (type.equals(property.path("type").asText())) {
      return true;
    }

    return StreamSupport.stream(property.path("type").spliterator(), false)
        .map(JsonNode::asText)
        .anyMatch(type::equals);
  }

  private static void assertArray(JsonNode property) {
    assertThat(property.path("type").asText()).isEqualTo("array");
    assertThat(property.path("items").isMissingNode()).isFalse();
  }

  private static void assertEnumProperty(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String expectedValue
  ) {
    final var schema = resolveSchema(openApi, schemaProperty(openApi, schemaName, propertyName));

    assertThat(schema.path("type").asText()).isEqualTo("string");
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .contains(expectedValue);
  }

  private static JsonNode resolveSchema(JsonNode openApi, JsonNode schema) {
    final var ref = schema.path("$ref").asText();
    if (ref.isBlank()) {
      return schema;
    }

    final var schemaName = ref.substring(ref.lastIndexOf('/') + 1);
    final var resolved = openApi.path("components").path("schemas").path(schemaName);

    assertThat(resolved.isMissingNode())
        .as("%s schema exists", schemaName)
        .isFalse();
    return resolved;
  }
}
