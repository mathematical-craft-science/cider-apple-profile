package org.craft.mcs.cap.shared.domain.classification.profile;

import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityZone;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SugarAcidityZoneAssessorTest {

  private final ChemicalProfileAxisExtractor extractor = new ChemicalProfileAxisExtractor();
  private final SugarAcidityZoneAssessor assessor = new SugarAcidityZoneAssessor(
      BuiltInCiderClassificationThresholds.sugarAcidityZones()
  );

  @NonNull
  private static ChemicalProfile profile(
      @NonNull String specificGravity,
      @NonNull String acidity,
      @NonNull String tannin
  ) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(specificGravity)
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .tanninity(ExactMeasurement.of(TanninityValue.of(tannin)))
        .build();
  }

  @Test
  void assessesPerfectApples() {
    assertThat(assess(profile("1.060", "4.50", "1500")).zone())
        .isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void assessesSugarRichApples() {
    assertThat(assess(profile("1.060", "7.50", "1000")).zone())
        .isEqualTo(SugarAcidityZone.SUGAR_RICH_APPLES);
  }

  @Test
  void assessesLowAcidityApples() {
    assertThat(assess(profile("1.045", "4.49", "2500")).zone())
        .isEqualTo(SugarAcidityZone.LOW_ACIDITY_APPLES);
  }

  @Test
  void assessesMediumSugarApples() {
    assertThat(assess(profile("1.045", "4.50", "1000")).zone())
        .isEqualTo(SugarAcidityZone.MEDIUM_SUGAR_APPLES);
  }

  @Test
  void assessesLowSugarApples() {
    assertThat(assess(profile("1.044", "4.50", "1000")).zone())
        .isEqualTo(SugarAcidityZone.LOW_SUGAR_APPLES);
  }

  @Test
  void leavesHighSugarMediumAcidityLowTanninUnclassified() {
    final var assessment = assess(profile("1.060", "4.50", "1499"));

    assertThat(assessment.classified()).isFalse();
    assertThat(assessment.zone()).isNull();
  }

  @NonNull
  private SugarAcidityZoneAssessment assess(
      @NonNull ChemicalProfile profile
  ) {
    return assessor.assess(extractor.extract(profile)).orElseThrow();
  }
}
