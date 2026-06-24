package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAxisNormalizerTest {

  private final MinMaxProfileAxisNormalizer normalizer =
      MinMaxProfileAxisNormalizer.fullCiderProfileRange();

  @Test
  void normalizesGraphMinimumToZero() {
    assertThat(normalizer.normalize(
        ProfileEvaluationAxis.SUGAR,
        new BigDecimal("1.020")
    )).isEqualByComparingTo("0");
    assertThat(normalizer.normalize(
        ProfileEvaluationAxis.ACIDITY,
        new BigDecimal("0")
    )).isEqualByComparingTo("0");
  }

  @Test
  void normalizesGraphMaximumToOne() {
    assertThat(normalizer.normalize(
        ProfileEvaluationAxis.SUGAR,
        new BigDecimal("1.080")
    )).isEqualByComparingTo("1");
    assertThat(normalizer.normalize(
        ProfileEvaluationAxis.ACIDITY,
        new BigDecimal("10")
    )).isEqualByComparingTo("1");
  }

  @Test
  void normalizesTanninByDomainRange() {
    assertThat(normalizer.normalize(
        ProfileEvaluationAxis.TANNIN,
        new BigDecimal("2000")
    )).isEqualByComparingTo("0.1666666666666667");
  }

  @Test
  void effectiveRangeUsesPracticalCiderBlendingRanges() {
    final var effectiveRangeNormalizer = EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults();

    assertThat(effectiveRangeNormalizer.normalize(
        ProfileEvaluationAxis.TANNIN,
        new BigDecimal("2000")
    )).isEqualByComparingTo("0.5");
    assertThat(effectiveRangeNormalizer.normalize(
        ProfileEvaluationAxis.ACIDITY,
        new BigDecimal("5.5")
    )).isEqualByComparingTo("0.5");
  }

  @Test
  void sensitivityUnitNormalizerMeasuresDistanceInMeaningfulAxisUnits() {
    final var sensitivityNormalizer = SensitivityUnitProfileAxisNormalizer.ciderBlendingDefaults();

    assertThat(sensitivityNormalizer.normalize(
        ProfileEvaluationAxis.SUGAR,
        new BigDecimal("0.010")
    )).isEqualByComparingTo("2");
    assertThat(sensitivityNormalizer.normalize(
        ProfileEvaluationAxis.TANNIN,
        new BigDecimal("500")
    )).isEqualByComparingTo("2");
  }

  @Test
  void thresholdSegmentNormalizerMapsThresholdBoundariesToEqualSegments() {
    final var thresholdNormalizer = ThresholdSegmentProfileAxisNormalizer.from(BuiltInSugarAcidityZoneProfiles.thresholds());

    assertThat(thresholdNormalizer.normalize(
        ProfileEvaluationAxis.SUGAR,
        new BigDecimal("1.045")
    )).isEqualByComparingTo("0.3333333333333333");
    assertThat(thresholdNormalizer.normalize(
        ProfileEvaluationAxis.SUGAR,
        new BigDecimal("1.060")
    )).isEqualByComparingTo("0.6666666666666667");
    assertThat(thresholdNormalizer.normalize(
        ProfileEvaluationAxis.TANNIN,
        new BigDecimal("2500")
    )).isEqualByComparingTo("0.6666666666666667");
  }

  @Test
  void selectorReturnsRequestedBuiltInNormalizerStrategy() {
    final var selector = new BuiltInProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds());

    assertThat(selector.select(ProfileAxisNormalizerCode.MIN_MAX))
        .isInstanceOf(MinMaxProfileAxisNormalizer.class);
    assertThat(selector.select(ProfileAxisNormalizerCode.EFFECTIVE_RANGE))
        .isInstanceOf(EffectiveRangeProfileAxisNormalizer.class);
    assertThat(selector.select(ProfileAxisNormalizerCode.THRESHOLD_SEGMENT))
        .isInstanceOf(ThresholdSegmentProfileAxisNormalizer.class);
    assertThat(selector.select(ProfileAxisNormalizerCode.SENSITIVITY_UNIT))
        .isInstanceOf(SensitivityUnitProfileAxisNormalizer.class);
  }
}
