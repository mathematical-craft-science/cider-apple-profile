package org.craft.mcs.cap.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class AppleVarietySugarAcidityZoneResultTest {

  @Test
  void rejectsPlottableResultWithoutAssessment() {
    final var capabilityAssessment = capabilityAssessment(plottableProfile());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new AppleVarietySugarAcidityZoneResult(
            AppleVarietyId.newId(),
            AppleVarietyName.of("Kingston Black"),
            true,
            null,
            capabilityAssessment
        ));
  }

  @Test
  void allowsUnplottableResultWithoutAssessment() {
    final var capabilityAssessment = capabilityAssessment(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build());

    final var result = new AppleVarietySugarAcidityZoneResult(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        false,
        null,
        capabilityAssessment
    );

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
  }

  @Test
  void allowsPlottableResultWithAssessment() {
    final var profile = plottableProfile();
    final var result = new AppleVarietySugarAcidityZoneResult(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        true,
        assessment(profile),
        capabilityAssessment(profile)
    );

    assertThat(result.plottable()).isTrue();
    assertThat(result.assessment()).isNotNull();
  }

  private static ChemicalProfile plottableProfile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.0")))
        .build();
  }

  private static ChemicalProfileCapabilityAssessment capabilityAssessment(
      ChemicalProfile profile
  ) {
    return new ChemicalProfileCapabilityAssessor().assess(
        new ChemicalProfileAxisExtractor().extract(profile),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
  }

  private static org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment assessment(
      ChemicalProfile profile
  ) {
    return new RectangleSugarAcidityZoneAssessor(
        BuiltInSugarAcidityZoneProfiles.thresholds(),
        BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
    ).assess(new ChemicalProfileAxisExtractor().extract(profile)).orElseThrow();
  }
}
