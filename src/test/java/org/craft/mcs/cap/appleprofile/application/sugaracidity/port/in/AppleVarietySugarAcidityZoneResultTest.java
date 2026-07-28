package org.craft.mcs.cap.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;


class AppleVarietySugarAcidityZoneResultTest {

  @Test
  void rejectsMissingEvaluation() {
    assertThatThrownBy(() -> new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.newId(),
            AppleVarietyName.of("Kingston Black"),
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
        evaluation(ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build()),
        null
    );

    assertThat(result.evaluation().plottable()).isFalse();
    assertThat(result.evaluation().assessment()).isNull();
  }

  @Test
  void allowsPlottableResultWithAssessment() {
    final var profile = plottableProfile();
    final var result = new AppleVarietySugarAcidityZoneResult(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        evaluation(profile),
        null
    );

    assertThat(result.evaluation().plottable()).isTrue();
    assertThat(result.evaluation().assessment()).isNotNull();
  }

  private static ChemicalProfile plottableProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
        .build();
  }

  private static ChemicalProfileAssessment evaluation(
      ChemicalProfile profile
  ) {
    return chemicalProfileAssessmentAdapter().project(profile);
  }
}
