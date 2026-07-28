package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .calculatedCompositionCharacteristics;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;


class ChemicalProfileCompositionTest {

  @Test
  void derivesConcreteTotalVolumeFromAbsoluteSourceWeights() {
    final var composition = ChemicalProfileComposition.of(
        List.of(absoluteSource("a", "10"), absoluteSource("b", "20")),
        calculatedCompositionCharacteristics(),
        List.of()
    );

    assertThat(composition.totalVolume()).isEqualTo(VolumeValue.ofLiters("30"));
  }

  @Test
  void rejectsDeclaredTotalThatDiffersFromAbsoluteSourceWeights() {
    final var sources = List.of(absoluteSource("a", "10"), absoluteSource("b", "20"));

    assertThatThrownBy(() -> new ChemicalProfileComposition(
        sources,
        calculatedCompositionCharacteristics(),
        VolumeValue.ofLiters("31"),
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("composition total volume must equal the sum of absolute source volumes");
  }

  @Test
  void rejectsConcreteTotalForProportionBasedComposition() {
    final var sources = List.of(
        proportionSource("a", "0.4"),
        proportionSource("b", "0.6")
    );

    assertThatThrownBy(() -> new ChemicalProfileComposition(
        sources,
        calculatedCompositionCharacteristics(),
        VolumeValue.ofLiters("100"),
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("proportion-based composition cannot declare a concrete total volume");
  }

  @Test
  void rejectsMixedSourceWeightModesInCalculatedResult() {
    final var sources = List.of(
        absoluteSource("a", "10"),
        proportionSource("b", "0.5")
    );

    assertThatThrownBy(() -> new ChemicalProfileComposition(
        sources,
        calculatedCompositionCharacteristics(),
        null,
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("calculated composition cannot mix absolute volumes and proportions");
  }

  @Test
  void rejectsEmptyCalculatedComposition() {
    assertThatThrownBy(() -> new ChemicalProfileComposition(
        List.of(),
        calculatedCompositionCharacteristics(),
        null,
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("chemical profile composition must contain at least one source");
  }

  @Test
  void sourceCannotRequestMoreThanItsAvailableVolume() {
    assertThatThrownBy(() -> new ChemicalProfileCompositionSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", "a"),
        compositionSourceCharacteristics("1.050", "6.0", null, null),
        new AbsoluteCompositionVolume(VolumeValue.ofLiters("11")),
        VolumeValue.ofLiters("10")
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessageContaining("requested volume exceeds available volume");
  }

  private static ChemicalProfileCompositionSource absoluteSource(
      String id,
      String volume
  ) {
    return source(id, new AbsoluteCompositionVolume(VolumeValue.ofLiters(volume)));
  }

  private static ChemicalProfileCompositionSource proportionSource(
      String id,
      String proportion
  ) {
    return source(id, CompositionProportion.of(proportion));
  }

  private static ChemicalProfileCompositionSource source(
      String id,
      ChemicalProfileCompositionSourceWeight weight
  ) {
    return new ChemicalProfileCompositionSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics("1.050", "6.0", null, null),
        weight,
        null
    );
  }
}
