package org.craft.mcs.cap.infrastructure.configuration;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAnalysisGraphPropertiesBindingTest {

  @Test
  void bindsLowercaseGeometryValueToTypedProperty() {
    final var source = new MapConfigurationPropertySource(Map.of(
        "cap.profile-analysis.sugar-acidity.geometry", "ellipse"
    ));

    final var properties = new Binder(source)
        .bind(
            "cap.profile-analysis.sugar-acidity",
            Bindable.of(ProfileAnalysisGraphProperties.class)
        )
        .orElseThrow(AssertionError::new);

    assertThat(properties)
        .returns(
            ProfileAnalysisGraphProperties.GraphZoneGeometryType.ELLIPSE,
            ProfileAnalysisGraphProperties::geometry
        );
  }
}
