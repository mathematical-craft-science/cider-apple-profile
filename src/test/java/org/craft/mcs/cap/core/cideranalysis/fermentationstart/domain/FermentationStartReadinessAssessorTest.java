package org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FermentationStartReadinessAssessorTest {

  private final FermentationStartReadinessAssessor assessor = new FermentationStartReadinessAssessor();

  @Test
  void returnsReadyWhenSugarPhAndYanAreInHeuristicRange() {
    final var assessment = assessor.assess(profile("1.052", "3.45", "180"));

    assertThat(assessment.status()).isEqualTo(FermentationStartReadinessStatus.READY);
    assertThat(assessment.checks())
        .extracting(FermentationStartReadinessCheck::code)
        .containsExactly("SUGAR_PRESENT", "PH_ACCEPTABLE", "YAN_ACCEPTABLE");
  }

  @Test
  void returnsNotReadyWhenYanIsSeverelyLow() {
    final var assessment = assessor.assess(profile("1.052", "3.45", "60"));

    assertThat(assessment.status()).isEqualTo(FermentationStartReadinessStatus.NOT_READY);
    assertThat(assessment.checks())
        .anySatisfy(check -> {
          assertThat(check.code()).isEqualTo("YAN_SEVERELY_LOW");
          assertThat(check.severity()).isEqualTo(FermentationStartReadinessSeverity.BLOCKER);
          assertThat(check.value()).isEqualByComparingTo("60");
          assertThat(check.unit()).isEqualTo("mg N/L");
        });
  }

  @Test
  void returnsAttentionWhenYanIsMissing() {
    final var assessment = assessor.assess(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
        .ph(ExactMeasurement.of(PhValue.of("3.45")))
        .build());

    assertThat(assessment.status()).isEqualTo(FermentationStartReadinessStatus.NEEDS_ATTENTION);
    assertThat(assessment.checks())
        .anySatisfy(check -> {
          assertThat(check.code()).isEqualTo("YAN_MISSING");
          assertThat(check.severity()).isEqualTo(FermentationStartReadinessSeverity.WARNING);
        });
  }

  @Test
  void returnsNotReadyWhenSugarIsMissing() {
    final var assessment = assessor.assess(ChemicalProfile.builder()
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.2")))
        .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("180")))
        .build());

    assertThat(assessment.status()).isEqualTo(FermentationStartReadinessStatus.NOT_READY);
    assertThat(assessment.checks())
        .anySatisfy(check -> assertThat(check.code()).isEqualTo("SUGAR_MISSING"));
  }

  private static ChemicalProfile profile(String sugar, String ph, String yan) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(sugar))))
        .ph(ExactMeasurement.of(PhValue.of(ph)))
        .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of(yan)))
        .build();
  }
}
