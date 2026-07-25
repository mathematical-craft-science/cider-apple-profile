package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.MassBalanceChemicalProfileCompositionStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationMissingPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.YeastAssimilableNitrogenReadinessCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.EffectiveRangeProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.DistanceToTargetAreaObjectiveFunction;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DiscretizedBoundedChemicalProfileCompositionOptimizationStrategyTest {

  private final DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy strategy =
      new DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy();
  private final MassBalanceChemicalProfileCompositionStrategy calculator =
      new MassBalanceChemicalProfileCompositionStrategy();

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
        List.of(),
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
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 10),
        CompositionOptimizationMode.ABSOLUTE_VOLUME,
        VolumeValue.ofLiters("100")
    ), calculator);

    assertThat(result.evaluatedCandidates()).isZero();
    assertThat(result.candidates()).isEmpty();
  }

  @Test
  void ranksByObjectiveScoreWithYanReadinessPenalty() {
    final var result = strategy.optimize(new ChemicalProfileCompositionOptimizationRequest(
        List.of(
            source("low-yan-a", "1.050", "6.0", "60", null),
            source("low-yan-b", "1.050", "6.0", "60", null),
            source("ready-a", "1.052", "6.2", "200", null),
            source("ready-b", "1.052", "6.2", "200", null)
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        new DistanceToTargetAreaObjectiveFunction(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(new YeastAssimilableNitrogenReadinessCriterion(
            YeastAssimilableNitrogenValue.of("140"),
            null,
            new BigDecimal("10"),
            CompositionOptimizationMissingPolicy.PENALIZE_IF_MISSING,
            BigDecimal.ONE
        )),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ), calculator);

    assertThat(result.candidates()).isNotEmpty();
    assertThat(result.candidates().getFirst().objectiveAssessment().penalties()).isEmpty();
    assertThat(result.candidates().getFirst().distance().value()).isNotNull();
    assertThat(result.candidates().getFirst().distance().value()).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void excludesCandidatesWithMissingYanWhenPolicyRequiresIt() {
    final var result = strategy.optimize(new ChemicalProfileCompositionOptimizationRequest(
        List.of(
            source("without-yan", "1.050", "6.0", null),
            source("with-yan", "1.052", "6.2", "200", null)
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        new DistanceToTargetAreaObjectiveFunction(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(new YeastAssimilableNitrogenReadinessCriterion(
            YeastAssimilableNitrogenValue.of("140"),
            null,
            BigDecimal.ONE,
            CompositionOptimizationMissingPolicy.EXCLUDE_IF_MISSING,
            BigDecimal.ONE
        )),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ), calculator);

    assertThat(result.evaluatedCandidates()).isEqualTo(1);
    assertThat(result.candidates()).isEmpty();
  }

  @Test
  void rejectsDuplicateSourceReferencesBeforeOptimization() {
    assertThatThrownBy(() -> new ChemicalProfileCompositionOptimizationRequest(
        List.of(
            source("same", "1.050", "6.0", null),
            source("same", "1.052", "6.2", null)
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        new DistanceToTargetAreaObjectiveFunction(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ))
        .isInstanceOf(InvalidChemicalProfileCompositionOptimizationException.class)
        .hasMessageContaining("each source exactly once");
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
    return source(id, sugar, acidity, null, availableVolume);
  }

  private static @NonNull ChemicalProfileCompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String sugar,
      @NonNull String acidity,
      @Nullable String yeastAssimilableNitrogen,
      @Nullable VolumeValue availableVolume
  ) {
    return new ChemicalProfileCompositionOptimizationSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id),
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, sugar),
            axis(ChemicalAxis.ACIDITY, acidity),
            null,
            yeastAssimilableNitrogen == null
                ? null
                : axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, yeastAssimilableNitrogen)
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
        AxisValueKind.POINT,
        null,
        null
    );
  }
}
