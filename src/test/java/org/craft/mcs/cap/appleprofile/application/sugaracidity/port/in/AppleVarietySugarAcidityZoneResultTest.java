package org.craft.mcs.cap.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
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

  private static SugarAcidityProfileEvaluation evaluation(
      ChemicalProfile profile
  ) {
    final var capabilityAssessment = capabilityAssessment(profile);
    final var assessment = assessment(profile);
    return new SugarAcidityProfileEvaluation(
        new ChemicalProfileAxisExtractor().extract(profile),
        capabilityAssessment,
        assessment != null,
        assessment
    );
  }

  private static SugarAcidityZoneAssessment assessment(
      ChemicalProfile profile
  ) {
    return new RectangleSugarAcidityZoneAssessor(
        BuiltInSugarAcidityZoneProfiles.thresholds(),
        BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
    ).assess(new ChemicalProfileAxisExtractor().extract(profile)).orElse(null); // NOSONAR
  }
}
