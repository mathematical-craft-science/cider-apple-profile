package org.craft.mcs.cap.infrastructure.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;


class ConfigurationPropertiesMetadataTest {

  private static final String METADATA_RESOURCE = "META-INF/spring-configuration-metadata.json";

  @Test
  void describesEveryCapConfigurationProperty() throws IOException {
    final var metadata = readMetadata();
    final var capProperties = StreamSupport.stream(metadata.path("properties").spliterator(), false)
        .filter(property -> property.path("name").asText().startsWith("cap."))
        .toList();

    assertThat(capProperties)
        .isNotEmpty()
        .allSatisfy(property -> assertThat(property.path("description").asText())
            .as(property.path("name").asText())
            .isNotBlank());
  }

  @Test
  void providesExplicitHintsForConfigurableEnums() throws IOException {
    final var metadata = readMetadata();
    final var hintedProperties = StreamSupport.stream(metadata.path("hints").spliterator(), false)
        .map(hint -> hint.path("name").asText())
        .toList();

    assertThat(hintedProperties)
        .containsExactlyInAnyOrder(
            "cap.account.default-workspace.type",
            "cap.account.default-workspace.settings.default-acidity-basis",
            "cap.security.provider",
            "cap.profile-analysis.sugar-acidity.geometry"
        );
  }

  @Test
  void exposesMeasurementFreshnessOnlyAsWorkspaceProvisioningDefault() throws IOException {
    final var propertyNames = StreamSupport.stream(
            readMetadata().path("properties").spliterator(),
            false
        )
        .map(property -> property.path("name").asText())
        .filter(name -> name.startsWith("cap."))
        .toList();

    assertThat(propertyNames)
        .contains("cap.account.default-workspace.settings.measurement-stale-after")
        .noneMatch(name -> name.startsWith("cap.feedstock.current-profile.measurement-freshness"));
  }

  private static JsonNode readMetadata() throws IOException {
    try (var input = requireNonNull(
        ConfigurationPropertiesMetadataTest.class.getClassLoader().getResourceAsStream(METADATA_RESOURCE),
        METADATA_RESOURCE + " is missing"
    )) {
      return new ObjectMapper().readTree(input);
    }
  }
}
