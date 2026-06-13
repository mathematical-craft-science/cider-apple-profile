package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.MassBalanceChemicalProfileCompositionStrategy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.DistanceToTargetAreaObjectiveFunction;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.EffectiveRangeProfileAxisNormalizer;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DiscretizedBoundedChemicalProfileCompositionOptimizationStrategyTest {

  private final DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy strategy =
      new DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy();
  private final MassBalanceChemicalProfileCompositionStrategy calculator = new MassBalanceChemicalProfileCompositionStrategy();

  @Test
  void ranksDiscreteCompositionsByDistanceToTarget() {
    final var result = strategy.optimize(new ChemicalProfileCompositionOptimizationRequest(
        List.of(
            source("low-acid", "1.060", "3.0", null),
            source("sharp", "1.040", "9.0", null),
            source("middle", "1.055", "5.2", null)
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        new DistanceToTargetAreaObjectiveFunction(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ), calculator);

    assertThat(result.evaluatedCandidates()).isEqualTo(3);
    assertThat(result.candidates()).hasSize(3);
    assertThat(result.candidates().getFirst().distance().insideTarget()).isTrue();
    assertThat(result.candidates().getFirst().distance().value()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void skipsAbsoluteVolumeCandidatesThatExceedAvailableVolume() {
    final var result = strategy.optimize(new ChemicalProfileCompositionOptimizationRequest(
        List.of(
            source("limited", "1.060", "3.0", VolumeValue.ofLiters("10")),
            source("sharp", "1.040", "9.0", VolumeValue.ofLiters("100"))
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.045", "1.055", "6.0", "8.0"),
        new DistanceToTargetAreaObjectiveFunction(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        new DiscretizedCompositionOptimizationSettings(50, 10),
        CompositionOptimizationMode.ABSOLUTE_VOLUME,
        VolumeValue.ofLiters("100")
    ), calculator);

    assertThat(result.evaluatedCandidates()).isZero();
    assertThat(result.candidates()).isEmpty();
  }

  private static @NonNull ProfileTargetObjective target(
      @NonNull String sugarMin,
      @NonNull String sugarMax,
      @NonNull String acidityMin,
      @NonNull String acidityMax
  ) {
    return ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            new BigDecimal(sugarMin),
            new BigDecimal(sugarMax),
            new BigDecimal(acidityMin),
            new BigDecimal(acidityMax)
        )
    ));
  }

  private static @NonNull ChemicalProfileCompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String sugar,
      @NonNull String acidity,
      @Nullable VolumeValue availableVolume
  ) {
    return new ChemicalProfileCompositionOptimizationSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id),
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, sugar),
            axis(ChemicalAxis.ACIDITY, acidity),
            null
        ),
        availableVolume
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
}
