package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BlendProfileAxisNormalizerTest {

  private final MinMaxBlendProfileAxisNormalizer normalizer =
      MinMaxBlendProfileAxisNormalizer.fullCiderProfileRange();

  @Test
  void normalizesGraphMinimumToZero() {
    assertThat(normalizer.normalize(
        BlendProfileAxis.SUGAR,
        new BigDecimal("1.020")
    )).isEqualByComparingTo("0");
    assertThat(normalizer.normalize(
        BlendProfileAxis.ACIDITY,
        new BigDecimal("0")
    )).isEqualByComparingTo("0");
  }

  @Test
  void normalizesGraphMaximumToOne() {
    assertThat(normalizer.normalize(
        BlendProfileAxis.SUGAR,
        new BigDecimal("1.080")
    )).isEqualByComparingTo("1");
    assertThat(normalizer.normalize(
        BlendProfileAxis.ACIDITY,
        new BigDecimal("10")
    )).isEqualByComparingTo("1");
  }

  @Test
  void normalizesTanninByDomainRange() {
    assertThat(normalizer.normalize(
        BlendProfileAxis.TANNIN,
        new BigDecimal("2000")
    )).isEqualByComparingTo("0.5");
  }

  @Test
  void effectiveRangeUsesPracticalCiderBlendingRanges() {
    final var effectiveRangeNormalizer = EffectiveRangeBlendProfileAxisNormalizer.ciderBlendingDefaults();

    assertThat(effectiveRangeNormalizer.normalize(
        BlendProfileAxis.TANNIN,
        new BigDecimal("2000")
    )).isEqualByComparingTo("0.5");
    assertThat(effectiveRangeNormalizer.normalize(
        BlendProfileAxis.ACIDITY,
        new BigDecimal("5.5")
    )).isEqualByComparingTo("0.5");
  }

  @Test
  void sensitivityUnitNormalizerMeasuresDistanceInMeaningfulAxisUnits() {
    final var sensitivityNormalizer = SensitivityUnitBlendProfileAxisNormalizer.ciderBlendingDefaults();

    assertThat(sensitivityNormalizer.normalize(
        BlendProfileAxis.SUGAR,
        new BigDecimal("0.010")
    )).isEqualByComparingTo("2");
    assertThat(sensitivityNormalizer.normalize(
        BlendProfileAxis.TANNIN,
        new BigDecimal("500")
    )).isEqualByComparingTo("2");
  }

  @Test
  void thresholdSegmentNormalizerMapsThresholdBoundariesToEqualSegments() {
    final var thresholdNormalizer = ThresholdSegmentBlendProfileAxisNormalizer.from(BuiltInSugarAcidityZoneProfiles.thresholds());

    assertThat(thresholdNormalizer.normalize(
        BlendProfileAxis.SUGAR,
        new BigDecimal("1.045")
    )).isEqualByComparingTo("0.3333333333333333");
    assertThat(thresholdNormalizer.normalize(
        BlendProfileAxis.SUGAR,
        new BigDecimal("1.060")
    )).isEqualByComparingTo("0.6666666666666667");
    assertThat(thresholdNormalizer.normalize(
        BlendProfileAxis.TANNIN,
        new BigDecimal("2500")
    )).isEqualByComparingTo("0.6666666666666667");
  }

  @Test
  void selectorReturnsRequestedBuiltInNormalizerStrategy() {
    final var selector = new BuiltInBlendProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds());

    assertThat(selector.select(BlendProfileAxisNormalizerCode.MIN_MAX))
        .isInstanceOf(MinMaxBlendProfileAxisNormalizer.class);
    assertThat(selector.select(BlendProfileAxisNormalizerCode.EFFECTIVE_RANGE))
        .isInstanceOf(EffectiveRangeBlendProfileAxisNormalizer.class);
    assertThat(selector.select(BlendProfileAxisNormalizerCode.THRESHOLD_SEGMENT))
        .isInstanceOf(ThresholdSegmentBlendProfileAxisNormalizer.class);
    assertThat(selector.select(BlendProfileAxisNormalizerCode.SENSITIVITY_UNIT))
        .isInstanceOf(SensitivityUnitBlendProfileAxisNormalizer.class);
  }
}
