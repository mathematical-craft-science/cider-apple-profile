package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MassBalanceChemicalProfileCompositionStrategyTest {

  private final MassBalanceChemicalProfileCompositionStrategy calculator = new MassBalanceChemicalProfileCompositionStrategy();

  @Test
  void calculatesCompositionFromAbsoluteVolumes() {
    final var result = calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", "1000", "120"), volume("10"), volume("20")),
        source("b", axes("1.045", "8.0", "2500", "210"), volume("20"), volume("30"))
    )));

    assertThat(result.profile().sugar().value()).isEqualByComparingTo("1.050000000");
    assertThat(result.profile().acidity().value()).isEqualByComparingTo("7.000000000");
    assertThat(result.profile().tannin()).isNotNull();
    assertThat(result.profile().tannin().value()).isEqualByComparingTo("2000.000000000");
    assertThat(result.yeastAssimilableNitrogen()).isNotNull();
    assertThat(result.yeastAssimilableNitrogen().value()).isEqualByComparingTo("180.000000000");
    assertThat(result.profile().sugar().valueKind()).isEqualTo(MeasurementValueKind.CALCULATED);
    assertThat(result.totalVolume()).isNotNull();
    assertThat(result.totalVolume().liters()).isEqualByComparingTo("30");
    assertThat(result.warnings()).isEmpty();
  }

  @Test
  void calculatesCompositionFromProportions() {
    final var result = calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", null), CompositionProportion.of("0.25")),
        source("b", axes("1.040", "9.0", null), CompositionProportion.of("0.75"))
    )));

    assertThat(result.profile().sugar().value()).isEqualByComparingTo("1.045000000");
    assertThat(result.profile().acidity().value()).isEqualByComparingTo("8.000000000");
    assertThat(result.profile().tannin()).isNull();
    assertThat(result.totalVolume()).isNull();
    assertThat(result.warnings()).isEmpty();
  }

  @Test
  void ignoresTanninWhenRequested() {
    final var result = calculator.calculate(new ChemicalProfileCompositionRequest(
        List.of(
            source("a", axes("1.060", "5.0", "1000"), CompositionProportion.of("1")),
            source("b", axes("1.040", "9.0", "2000"), CompositionProportion.of("1"))
        ),
        ChemicalProfileCompositionTanninPolicy.IGNORE_TANNIN
    ));

    assertThat(result.profile().tannin()).isNull();
    assertThat(result.warnings()).containsExactly(ChemicalProfileCompositionWarning.TANNIN_IGNORED);
  }

  @Test
  void warnsWhenTanninIsOnlyPartiallyAvailable() {
    final var result = calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", "1000"), CompositionProportion.of("1")),
        source("b", axes("1.040", "9.0", null), CompositionProportion.of("1"))
    )));

    assertThat(result.profile().tannin()).isNull();
    assertThat(result.warnings()).containsExactly(ChemicalProfileCompositionWarning.TANNIN_INCOMPLETE);
  }

  @Test
  void warnsWhenYanIsOnlyPartiallyAvailable() {
    final var result = calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", null, "120"), CompositionProportion.of("1")),
        source("b", axes("1.040", "9.0", null, null), CompositionProportion.of("1"))
    )));

    assertThat(result.yeastAssimilableNitrogen()).isNull();
    assertThat(result.warnings())
        .containsExactly(ChemicalProfileCompositionWarning.YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE);
  }

  @Test
  void rejectsMissingRequiredSugarOrAcidity() {
    assertThatThrownBy(() -> calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("missing-sugar", ChemicalProfileAxes.of(null, axis(ChemicalAxis.ACIDITY, "5.0"), null), CompositionProportion.of("1")),
        source("complete", axes("1.040", "9.0", null), CompositionProportion.of("1"))
    ))))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("missing required axis SUGAR")
        .hasMessageContaining("missing-sugar");

    assertThatThrownBy(() -> calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("missing-acidity", ChemicalProfileAxes.of(axis(ChemicalAxis.SUGAR, "1.050"), null, null), CompositionProportion.of("1")),
        source("complete", axes("1.040", "9.0", null), CompositionProportion.of("1"))
    ))))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("missing required axis ACIDITY")
        .hasMessageContaining("missing-acidity");
  }

  @Test
  void rejectsRequiredTanninWhenAnySourceIsMissingTannin() {
    assertThatThrownBy(() -> calculator.calculate(new ChemicalProfileCompositionRequest(
        List.of(
            source("a", axes("1.060", "5.0", "1000"), CompositionProportion.of("1")),
            source("b", axes("1.040", "9.0", null), CompositionProportion.of("1"))
        ),
        ChemicalProfileCompositionTanninPolicy.REQUIRE_TANNIN
    )))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("missing required axis TANNINITY")
        .hasMessageContaining("b");
  }

  @Test
  void rejectsMixedWeightModes() {
    assertThatThrownBy(() -> calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", null), CompositionProportion.of("1")),
        source("b", axes("1.040", "9.0", null), volume("10"), null)
    ))))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("cannot mix absolute volumes and proportions");
  }

  @Test
  void rejectsAbsoluteVolumeAboveAvailableVolume() {
    assertThatThrownBy(() -> calculator.calculate(ChemicalProfileCompositionRequest.calculateTanninWhenComplete(List.of(
        source("a", axes("1.060", "5.0", null), volume("25"), volume("20")),
        source("b", axes("1.040", "9.0", null), volume("10"), volume("30"))
    ))))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("requested volume exceeds available volume")
        .hasMessageContaining("a");
  }

  private static @NonNull ChemicalProfileCompositionSource source(
      @NonNull String id,
      @NonNull ChemicalProfileAxes axes,
      @NonNull VolumeValue volume,
      @Nullable VolumeValue availableVolume
  ) {
    return new ChemicalProfileCompositionSource(reference(id), axes, new AbsoluteCompositionVolume(volume), availableVolume);
  }

  private static @NonNull ChemicalProfileCompositionSource source(
      @NonNull String id,
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalProfileCompositionSourceWeight weight
  ) {
    return new ChemicalProfileCompositionSource(reference(id), axes, weight, null);
  }

  private static @NonNull ChemicalProfileCompositionSourceReference reference(@NonNull String id) {
    return new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id);
  }

  private static @NonNull ChemicalProfileAxes axes(
      @NonNull String sugar,
      @NonNull String acidity,
      @Nullable String tannin
  ) {
    return axes(sugar, acidity, tannin, null);
  }

  private static @NonNull ChemicalProfileAxes axes(
      @NonNull String sugar,
      @NonNull String acidity,
      @Nullable String tannin,
      @Nullable String yeastAssimilableNitrogen
  ) {
    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, sugar),
        axis(ChemicalAxis.ACIDITY, acidity),
        tannin == null ? null : axis(ChemicalAxis.TANNINITY, tannin),
        yeastAssimilableNitrogen == null
            ? null
            : axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, yeastAssimilableNitrogen)
    );
  }

  private static @NonNull AxisValue axis(
      @NonNull ChemicalAxis axis,
      @NonNull String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }

  private static @NonNull VolumeValue volume(@NonNull String liters) {
    return VolumeValue.ofLiters(liters);
  }
}
