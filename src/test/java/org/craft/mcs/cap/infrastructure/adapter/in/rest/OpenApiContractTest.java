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

  private static void assertStringFormat(JsonNode property, String format) {
    assertThat(property.path("type").asText()).isEqualTo("string");
    assertThat(property.path("format").asText()).isEqualTo(format);
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
