package org.craft.mcs.cap.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;


class AppleVarietySugarAcidityZoneResultTest {

  @Test
  void rejectsMissingEvaluation() {
    assertThatThrownBy(() -> new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.newId(),
            AppleVarietyName.of("Kingston Black"),
            null,
            null,
            null,
            null,
            null
        ))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void allowsUnplottableResultWithoutAssessment() {
    final var result = new AppleVarietySugarAcidityZoneResult(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        specificGravity("1.050"),
        null,
        evaluation(specificGravity("1.050"), null),
        null,
        null
    );

    assertThat(result.graphEvaluation().plottable()).isFalse();
    assertThat(result.graphEvaluation().zoneAssessment()).isNull();
  }

  @Test
  void allowsPlottableResultWithAssessment() {
    final var specificGravity = specificGravity("1.060");
    final var titratableAcidity = PointCharacteristicValue.of(TitratableAcidityValue.of("5.0"));
    final var result = new AppleVarietySugarAcidityZoneResult(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        specificGravity,
        titratableAcidity,
        evaluation(specificGravity, titratableAcidity),
        null,
        null
    );

    assertThat(result.graphEvaluation().plottable()).isTrue();
    assertThat(result.graphEvaluation().zoneAssessment()).isNotNull();
  }

  private static PointCharacteristicValue<SpecificGravityValue> specificGravity(String value) {
    return PointCharacteristicValue.of(SpecificGravityValue.of(value));
  }

  private static JolicoeurSugarAcidityGraphEvaluation evaluation(
      PointCharacteristicValue<SpecificGravityValue> specificGravity,
      PointCharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    return graphEvaluator().evaluate(specificGravity, titratableAcidity);
  }
}
