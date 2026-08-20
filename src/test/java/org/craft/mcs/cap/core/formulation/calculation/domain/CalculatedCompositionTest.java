package org.craft.mcs.cap.core.formulation.calculation.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures
    .calculatedCompositionCharacteristics;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures
    .compositionSourceCharacteristics;


class CalculatedCompositionTest {

  @Test
  void derivesConcreteTotalVolumeFromAbsoluteSourceWeights() {
    final var composition = CalculatedComposition.of(
        List.of(absoluteSource("a", "10"), absoluteSource("b", "20")),
        calculatedCompositionCharacteristics(),
        List.of()
    );

    assertThat(composition.totalVolume()).isEqualTo(VolumeValue.ofLiters("30"));
  }

  @Test
  void rejectsDeclaredTotalThatDiffersFromAbsoluteSourceWeights() {
    final var sources = List.of(absoluteSource("a", "10"), absoluteSource("b", "20"));

    assertThatThrownBy(() -> new CalculatedComposition(
        sources,
        calculatedCompositionCharacteristics(),
        VolumeValue.ofLiters("31"),
        List.of()
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition total volume must equal the sum of absolute source volumes");
  }

  @Test
  void rejectsConcreteTotalForProportionBasedComposition() {
    final var sources = List.of(
        proportionSource("a", "0.4"),
        proportionSource("b", "0.6")
    );

    assertThatThrownBy(() -> new CalculatedComposition(
        sources,
        calculatedCompositionCharacteristics(),
        VolumeValue.ofLiters("100"),
        List.of()
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("proportion-based composition cannot declare a concrete total volume");
  }

  @Test
  void rejectsMixedSourceWeightModesInCalculatedResult() {
    final var sources = List.of(
        absoluteSource("a", "10"),
        proportionSource("b", "0.5")
    );

    assertThatThrownBy(() -> new CalculatedComposition(
        sources,
        calculatedCompositionCharacteristics(),
        null,
        List.of()
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition cannot mix absolute volumes and proportions");
  }

  @Test
  void rejectsMixedSourceWeightModesThroughFactory() {
    final var sources = List.of(
        absoluteSource("a", "10"),
        proportionSource("b", "0.5")
    );
    final var characteristics = calculatedCompositionCharacteristics();
    final var warnings = List.<CompositionWarning>of();

    assertThatThrownBy(() -> CalculatedComposition.of(
        sources,
        characteristics,
        warnings
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition cannot mix absolute volumes and proportions");
  }

  @Test
  void rejectsEmptyCalculatedComposition() {
    assertThatThrownBy(() -> new CalculatedComposition(
        List.of(),
        calculatedCompositionCharacteristics(),
        null,
        List.of()
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition must contain at least one source");
  }

  @Test
  void rejectsEmptyCalculatedCompositionThroughFactory() {
    final var sources = List.<CompositionSource>of();
    final var characteristics = calculatedCompositionCharacteristics();
    final var warnings = List.<CompositionWarning>of();

    assertThatThrownBy(() -> CalculatedComposition.of(
        sources,
        characteristics,
        warnings
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition must contain at least one source");
  }

  @Test
  void sourceCannotRequestMoreThanItsAvailableVolume() {
    assertThatThrownBy(() -> new CompositionSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", "a"),
        compositionSourceCharacteristics("1.050", "6.0", null, null),
        new AbsoluteCompositionVolume(VolumeValue.ofLiters("11")),
        VolumeValue.ofLiters("10")
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessageContaining("requested volume exceeds available volume");
  }

  @Test
  void rejectsCalculatedResultWithNonNormalizedProportions() {
    final var sources = List.of(
        proportionSource("a", "0.4"),
        proportionSource("b", "0.5")
    );

    assertThatThrownBy(() -> new CalculatedComposition(
        sources,
        calculatedCompositionCharacteristics(),
        null,
        List.of()
    ))
        .isInstanceOf(InvalidCompositionException.class)
        .hasMessage("composition proportions must sum to one");
  }

  private static CompositionSource absoluteSource(
      String id,
      String volume
  ) {
    return source(id, new AbsoluteCompositionVolume(VolumeValue.ofLiters(volume)));
  }

  private static CompositionSource proportionSource(
      String id,
      String proportion
  ) {
    return source(id, CompositionProportion.of(proportion));
  }

  private static CompositionSource source(
      String id,
      CompositionSourceWeight weight
  ) {
    return new CompositionSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics("1.050", "6.0", null, null),
        weight,
        null
    );
  }
}
