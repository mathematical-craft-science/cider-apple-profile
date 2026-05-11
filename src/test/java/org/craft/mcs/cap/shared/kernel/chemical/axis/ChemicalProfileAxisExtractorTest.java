package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileAxisExtractorTest {

  private final ChemicalProfileAxisExtractor extractor = new ChemicalProfileAxisExtractor();

  @Test
  void extractsExactSugarAxisInSpecificGravity() {
    final var axes = extractor.extract(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build());

    final var sugar = axes.valueOf(ChemicalAxis.SUGAR).orElseThrow();

    assertThat(sugar.value()).isEqualByComparingTo("1.052");
    assertThat(sugar.valueKind()).isEqualTo(MeasurementValueKind.EXACT);
    assertThat(axes.has(ChemicalAxis.ACIDITY)).isFalse();
  }

  @Test
  void extractsRangeMidpointForAcidity() {
    final var axes = extractor.extract(ChemicalProfile.builder()
        .titratableAcidity(RangeMeasurement.of(
            TitratableAcidityValue.of("4.0"),
            TitratableAcidityValue.of("6.0")
        ))
        .build());

    final var acidity = axes.valueOf(ChemicalAxis.ACIDITY).orElseThrow();

    assertThat(acidity.value()).isEqualByComparingTo("5.000000");
    assertThat(acidity.min()).isEqualByComparingTo("4.0");
    assertThat(acidity.max()).isEqualByComparingTo("6.0");
    assertThat(acidity.valueKind()).isEqualTo(MeasurementValueKind.RANGE_MIDPOINT);
  }

  @Test
  void extractsStatisticalMedianBeforeMeanAndKeepsStatistics() {
    final var axes = extractor.extract(ChemicalProfile.builder()
        .titratableAcidity(StatisticalMeasurement.of(
            TitratableAcidityValue.of("5.0"),
            TitratableAcidityValue.of("4.8"),
            new BigDecimal("0.4"),
            8
        ))
        .build());

    final var acidity = axes.valueOf(ChemicalAxis.ACIDITY).orElseThrow();

    assertThat(acidity.value()).isEqualByComparingTo("4.8");
    assertThat(acidity.valueKind()).isEqualTo(MeasurementValueKind.STATISTICAL_MEDIAN);
    assertThat(acidity.standardDeviation()).isEqualByComparingTo("0.4");
    assertThat(acidity.sampleSize()).isEqualTo(8);
  }
}
