package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SugarAcidityZoneAssessorTest {

  private final ChemicalProfileAxisExtractor extractor = new ChemicalProfileAxisExtractor();
  private final SugarAcidityZoneAssessor assessor = new RectangleSugarAcidityZoneAssessor(
      BuiltInSugarAcidityZoneProfiles.thresholds(),
      BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
  );

  @NonNull
  private static ChemicalProfile profile(
      @NonNull String specificGravity,
      @NonNull String acidity,
      @NonNull String tannin
  ) {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(specificGravity)
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)))
        .tannin(PointCharacteristicValue.of(TanninValue.of(tannin)))
        .build();
  }

  @Test
  void assessesPerfectApples() {
    final var assessment = assess(profile("1.060", "4.50", "1500"));

    assertThat(assessment.zone())
        .isEqualTo(SugarAcidityZone.PERFECT_APPLES);
    assertThat(assessment.balancedForSingleVariety()).isTrue();
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
  void assessesPerfectAppleZoneSeparatelyFromSingleVarietyBalance() {
    final var assessment = assess(profile("1.060", "4.50", "1499"));

    assertThat(assessment.classified()).isTrue();
    assertThat(assessment.zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
    assertThat(assessment.balancedForSingleVariety()).isFalse();
  }

  @Test
  void exposesGraphPointValues() {
    final var assessment = assess(ChemicalProfile.builder()
        .sugar(IntervalCharacteristicValue.of(
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.050")),
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060"))
        ))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
        .tannin(PointCharacteristicValue.of(TanninValue.of("1800")))
        .build());

    assertThat(assessment.point().sugar().value()).isEqualByComparingTo("1.055000");
    assertThat(assessment.point().sugar().min()).isEqualByComparingTo("1.050");
    assertThat(assessment.point().sugar().max()).isEqualByComparingTo("1.060");
    assertThat(assessment.point().sugar().valueKind()).isEqualTo(AxisValueKind.INTERVAL_MIDPOINT);
    assertThat(assessment.point().acidity().value()).isEqualByComparingTo("5.0");
    assertThat(assessment.point().tannin()).isNotNull();
    assertThat(assessment.point().tannin().value()).isEqualByComparingTo("1800");
  }

  @Test
  void ellipticalAssessorUsesEllipseGeometry() {
    final var ellipticalAssessor = new EllipseSugarAcidityZoneAssessor(
        BuiltInSugarAcidityZoneProfiles.thresholds(),
        BuiltInSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
    );

    final var assessment = ellipticalAssessor.assess(
        extractor.extract(profile("1.065", "5.9", "1800"))
    ).orElseThrow();

    assertThat(assessment.zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
    assertThat(assessment.balancedForSingleVariety()).isTrue();
  }

  @NonNull
  private SugarAcidityZoneAssessment assess(
      @NonNull ChemicalProfile profile
  ) {
    return assessor.assess(extractor.extract(profile)).orElseThrow();
  }
}
