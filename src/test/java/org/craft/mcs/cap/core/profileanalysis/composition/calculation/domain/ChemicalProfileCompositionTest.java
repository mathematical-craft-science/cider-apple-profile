package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileCompositionTest {

  private static final SugarAcidityProfilePoint PROFILE = new SugarAcidityProfilePoint(
      AxisValue.calculated(ChemicalAxis.SUGAR, new BigDecimal("1.050")),
      AxisValue.calculated(ChemicalAxis.ACIDITY, new BigDecimal("6.0")),
      null
  );

  @Test
  void derivesConcreteTotalVolumeFromAbsoluteSourceWeights() {
    final var composition = ChemicalProfileComposition.of(
        List.of(absoluteSource("a", "10"), absoluteSource("b", "20")),
        PROFILE,
        null,
        List.of()
    );

    assertThat(composition.totalVolume())
        .isEqualTo(VolumeValue.ofLiters("30"));
  }

  @Test
  void rejectsDeclaredTotalThatDiffersFromAbsoluteSourceWeights() {
    final var sources = List.of(absoluteSource("a", "10"), absoluteSource("b", "20"));

    assertThatThrownBy(() -> new ChemicalProfileComposition(
        sources,
        PROFILE,
        null,
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
        PROFILE,
        null,
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
        PROFILE,
        null,
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
        PROFILE,
        null,
        null,
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("chemical profile composition must contain at least one source");
  }

  @Test
  void rejectsNonYanAxisInYeastAssimilableNitrogenField() {
    final var sources = List.of(absoluteSource("a", "10"));
    final var tannin = AxisValue.calculated(ChemicalAxis.TANNIN, new BigDecimal("1200"));

    assertThatThrownBy(() -> ChemicalProfileComposition.of(
        sources,
        PROFILE,
        tannin,
        List.of()
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionException.class)
        .hasMessage("composition yeast assimilable nitrogen must use YEAST_ASSIMILABLE_NITROGEN axis");
  }

  @Test
  void sourceCannotRequestMoreThanItsAvailableVolume() {
    assertThatThrownBy(() -> new ChemicalProfileCompositionSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", "a"),
        axes(),
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
        axes(),
        weight,
        null
    );
  }

  private static ChemicalProfileAxes axes() {
    return ChemicalProfileAxes.of(
        AxisValue.point(ChemicalAxis.SUGAR, new BigDecimal("1.050")),
        AxisValue.point(ChemicalAxis.ACIDITY, new BigDecimal("6.0")),
        null
    );
  }
}
