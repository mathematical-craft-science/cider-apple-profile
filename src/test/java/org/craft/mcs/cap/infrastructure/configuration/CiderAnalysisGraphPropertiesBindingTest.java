package org.craft.mcs.cap.infrastructure.configuration;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


class CiderAnalysisGraphPropertiesBindingTest {

  @Test
  void bindsLowercaseGeometryValueToTypedProperty() {
    final var source = new MapConfigurationPropertySource(Map.of(
        "cap.cider-analysis.sugar-acidity.geometry", "ellipse"
    ));

    final var properties = new Binder(source)
        .bind(
            "cap.cider-analysis.sugar-acidity",
            Bindable.of(CiderAnalysisGraphProperties.class)
        )
        .orElseThrow(AssertionError::new);

    assertThat(properties)
        .returns(
            CiderAnalysisGraphProperties.GraphZoneGeometryType.ELLIPSE,
            CiderAnalysisGraphProperties::geometry
        );
  }
}
