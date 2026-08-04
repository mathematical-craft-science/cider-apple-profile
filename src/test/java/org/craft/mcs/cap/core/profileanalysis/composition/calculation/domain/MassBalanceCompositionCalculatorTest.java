package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;


class MassBalanceCompositionCalculatorTest {

  private final MassBalanceCompositionCalculator calculator =
      new MassBalanceCompositionCalculator();

  @Test
  void calculatesCompositionFromAbsoluteVolumes() {
    final var result = calculator.calculate(CompositionCalculationRequest.calculateTanninWhenComplete(List.of(
        source("a", characteristics("1.060", "5.0", "1000", "120"), volume("10"), volume("20")),
        source("b", characteristics("1.045", "8.0", "2500", "210"), volume("20"), volume("30"))
    )));

    assertThat(result)
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.specificGravity().asBigDecimal())
                .isEqualByComparingTo("1.050000000"))
            .satisfies(characteristics -> assertThat(characteristics.titratableAcidity().asBigDecimal())
                .isEqualByComparingTo("7.000000000"))
            .satisfies(characteristics -> assertThat(characteristics.tannin())
                .isNotNull()
                .satisfies(tannin -> assertThat(tannin.asBigDecimal())
                    .isEqualByComparingTo("2000.000000000")))
            .satisfies(characteristics -> assertThat(characteristics.yeastAssimilableNitrogen())
                .isNotNull()
                .satisfies(yan -> assertThat(yan.asBigDecimal())
                    .isEqualByComparingTo("180.000000000"))))
        .satisfies(value -> assertThat(value.totalVolume())
            .isEqualTo(VolumeValue.ofLiters("30")))
        .satisfies(value -> assertThat(value.warnings()).isEmpty());
  }

  @Test
  void calculatesCompositionFromProportions() {
    final var result = calculator.calculate(CompositionCalculationRequest.calculateTanninWhenComplete(List.of(
        source("a", characteristics("1.060", "5.0", null, null), CompositionProportion.of("0.25")),
        source("b", characteristics("1.040", "9.0", null, null), CompositionProportion.of("0.75"))
    )));

    assertThat(result)
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.specificGravity().asBigDecimal())
                .isEqualByComparingTo("1.045000000"))
            .satisfies(characteristics -> assertThat(characteristics.titratableAcidity().asBigDecimal())
                .isEqualByComparingTo("8.000000000"))
            .satisfies(characteristics -> assertThat(characteristics.tannin()).isNull()))
        .satisfies(value -> assertThat(value.totalVolume()).isNull())
        .satisfies(value -> assertThat(value.warnings()).isEmpty());
  }

  @Test
  void usesIntervalMidpointsAccordingToExplicitPolicy() {
    final var interval = new CompositionSourceCharacteristics(
        IntervalCharacteristicValue.of(
            SpecificGravityValue.of("1.040"),
            SpecificGravityValue.of("1.060")
        ),
        IntervalCharacteristicValue.of(
            TitratableAcidityValue.of("4.0"),
            TitratableAcidityValue.of("8.0")
        ),
        null,
        null
    );

    final var result = calculator.calculate(new CompositionCalculationRequest(
        List.of(source("interval", interval, CompositionProportion.of("1"))),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        CompositionIntervalPolicy.MIDPOINT
    ));

    assertThat(result.characteristics())
        .satisfies(value -> assertThat(value.specificGravity().asBigDecimal())
            .isEqualByComparingTo("1.050000000"))
        .satisfies(value -> assertThat(value.titratableAcidity().asBigDecimal())
            .isEqualByComparingTo("6.000000000"));
  }

  @Test
  void ignoresTanninWhenRequested() {
    final var result = calculator.calculate(new CompositionCalculationRequest(
        List.of(
            source("a", characteristics("1.060", "5.0", "1000", null), CompositionProportion.of("0.5")),
            source("b", characteristics("1.040", "9.0", "2000", null), CompositionProportion.of("0.5"))
        ),
        CompositionTanninPolicy.IGNORE_TANNIN
    ));

    assertThat(result)
        .satisfies(value -> assertThat(value.characteristics().tannin()).isNull())
        .satisfies(value -> assertThat(value.warnings())
            .containsExactly(CompositionWarning.TANNIN_IGNORED));
  }

  @Test
  void warnsWhenOptionalCharacteristicsAreOnlyPartiallyAvailable() {
    final var result = calculator.calculate(CompositionCalculationRequest.calculateTanninWhenComplete(List.of(
        source("a", characteristics("1.060", "5.0", "1000", "120"), CompositionProportion.of("0.5")),
        source("b", characteristics("1.040", "9.0", null, null), CompositionProportion.of("0.5"))
    )));

    assertThat(result)
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.tannin()).isNull())
            .satisfies(characteristics -> assertThat(characteristics.yeastAssimilableNitrogen()).isNull()))
        .satisfies(value -> assertThat(value.warnings()).containsExactly(
            CompositionWarning.TANNIN_INCOMPLETE,
            CompositionWarning.YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE
        ));
  }

  @Test
  void rejectsRequiredTanninWhenAnySourceIsMissingTannin() {
    assertThatThrownBy(() -> calculator.calculate(new CompositionCalculationRequest(
        List.of(
            source("a", characteristics("1.060", "5.0", "1000", null), CompositionProportion.of("0.5")),
            source("b", characteristics("1.040", "9.0", null, null), CompositionProportion.of("0.5"))
        ),
        CompositionTanninPolicy.REQUIRE_TANNIN
    )))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessageContaining("missing required tannin")
        .hasMessageContaining("b");
  }

  @Test
  void rejectsMixedWeightModes() {
    assertThatThrownBy(() -> CompositionCalculationRequest.calculateTanninWhenComplete(List.of(
        source("a", characteristics("1.060", "5.0", null, null), CompositionProportion.of("1")),
        source("b", characteristics("1.040", "9.0", null, null), volume("10"), null)
    )))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessageContaining("cannot mix absolute volumes and proportions");
  }

  @Test
  void rejectsAbsoluteVolumeAboveAvailableVolume() {
    assertThatThrownBy(() -> source(
        "a",
        characteristics("1.060", "5.0", null, null),
        volume("25"),
        volume("20")
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessageContaining("requested volume exceeds available volume")
        .hasMessageContaining("a");
  }

  @Test
  void rejectsDuplicateSourceReferences() {
    assertThatThrownBy(() -> new CompositionCalculationRequest(
        List.of(
            source("same", characteristics("1.060", "5.0", null, null), CompositionProportion.of("0.5")),
            source("same", characteristics("1.040", "9.0", null, null), CompositionProportion.of("0.5"))
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessageContaining("each source exactly once");
  }

  @Test
  void rejectsProportionsThatDoNotSumToOne() {
    assertThatThrownBy(() -> new CompositionCalculationRequest(
        List.of(
            source("a", characteristics("1.060", "5.0", null, null), CompositionProportion.of("0.4")),
            source("b", characteristics("1.040", "9.0", null, null), CompositionProportion.of("0.5"))
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition proportions must sum to one");
  }

  @Test
  void rejectsProportionGreaterThanOne() {
    assertThatThrownBy(() -> CompositionProportion.of("1.01"))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition proportion must be greater than zero and at most one");
  }

  @Test
  void acceptsMaximumNormalizedProportion() {
    assertThat(CompositionProportion.of("1"))
        .extracting(CompositionProportion::value)
        .isEqualTo(BigDecimal.ONE);
  }

  @Test
  void rejectsZeroProportion() {
    assertThatThrownBy(() -> CompositionProportion.of("0"))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition proportion must be greater than zero and at most one");
  }

  private static @NonNull CompositionSourceCharacteristics characteristics(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin,
      @Nullable String yan
  ) {
    return compositionSourceCharacteristics(specificGravity, titratableAcidity, tannin, yan);
  }

  private static @NonNull CompositionSource source(
      @NonNull String id,
      @NonNull CompositionSourceCharacteristics characteristics,
      @NonNull VolumeValue volume,
      @Nullable VolumeValue availableVolume
  ) {
    return new CompositionSource(
        reference(id),
        characteristics,
        new AbsoluteCompositionVolume(volume),
        availableVolume
    );
  }

  private static @NonNull CompositionSource source(
      @NonNull String id,
      @NonNull CompositionSourceCharacteristics characteristics,
      @NonNull CompositionSourceWeight weight
  ) {
    return new CompositionSource(reference(id), characteristics, weight, null);
  }

  private static @NonNull CompositionSourceReference reference(@NonNull String id) {
    return new CompositionSourceReference("FEEDSTOCK_BATCH", id);
  }

  private static @NonNull VolumeValue volume(@NonNull String liters) {
    return VolumeValue.ofLiters(liters);
  }
}
