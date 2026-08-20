package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CompositionOptimizationSettingsTest {

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 4, 5, 10, 20, 25, 50})
  void acceptsStepPercentThatDividesOneHundred(int stepPercent) {
    assertThat(new CompositionOptimizationSettings(stepPercent, 10).stepPercent()).isEqualTo(stepPercent);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 3, 51})
  void rejectsUnsupportedStepPercent(int stepPercent) {
    assertThatThrownBy(() -> new CompositionOptimizationSettings(stepPercent, 10))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("optimization step percent must be between 1 and 50 and divide 100 exactly");
  }

  @Test
  void acceptsCandidateLimitBoundaries() {
    assertThat(new CompositionOptimizationSettings(5, 1).limit()).isEqualTo(1);
    assertThat(new CompositionOptimizationSettings(5, 100).limit()).isEqualTo(100);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 101})
  void rejectsCandidateLimitOutsideBoundaries(int limit) {
    assertThatThrownBy(() -> new CompositionOptimizationSettings(5, limit))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("optimization candidate limit must be between 1 and 100");
  }

  @Test
  void exposesStableDefaults() {
    assertThat(CompositionOptimizationSettings.defaults())
        .returns(5, CompositionOptimizationSettings::stepPercent)
        .returns(10, CompositionOptimizationSettings::limit);
  }
}
