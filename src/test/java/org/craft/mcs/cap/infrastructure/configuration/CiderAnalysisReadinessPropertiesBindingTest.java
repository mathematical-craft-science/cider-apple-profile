package org.craft.mcs.cap.infrastructure.configuration;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


class CiderAnalysisReadinessPropertiesBindingTest {

  @Test
  void bindsReadinessRulesFromCiderAnalysisNamespace() {
    final var source = new MapConfigurationPropertySource(Map.ofEntries(
        Map.entry("cap.cider-analysis.readiness.ph.generic-yeast-tolerance-min", "2.8"),
        Map.entry("cap.cider-analysis.readiness.ph.cider-guidance-min", "3.3"),
        Map.entry("cap.cider-analysis.readiness.ph.cider-guidance-max", "3.8"),
        Map.entry("cap.cider-analysis.readiness.ph.high-microbial-risk", "4.0"),
        Map.entry("cap.cider-analysis.readiness.ph.source-code", "PH_RULES"),
        Map.entry("cap.cider-analysis.readiness.ph.source-comment", "pH source"),
        Map.entry("cap.cider-analysis.readiness.yan.severe-low", "75"),
        Map.entry("cap.cider-analysis.readiness.yan.low", "150"),
        Map.entry("cap.cider-analysis.readiness.yan.high", "400"),
        Map.entry("cap.cider-analysis.readiness.yan.source-code", "YAN_RULES"),
        Map.entry("cap.cider-analysis.readiness.yan.source-comment", "YAN source")
    ));

    final var properties = new Binder(source)
        .bind(
            "cap.cider-analysis.readiness",
            Bindable.of(CiderAnalysisReadinessProperties.class)
        )
        .orElseThrow(AssertionError::new);

    assertThat(properties)
        .satisfies(value -> {
          assertThat(value.ph().sourceCode()).isEqualTo("PH_RULES");
          assertThat(value.yan().sourceCode()).isEqualTo("YAN_RULES");
        });
  }
}
