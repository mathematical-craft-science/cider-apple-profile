package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.MassBalanceCompositionCalculator;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationMissingPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.YeastAssimilableNitrogenReadinessCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedBoundedCompositionOptimizationStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.EffectiveRangeProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.TargetAreaProfileTargetObjectiveFunction;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;


class DiscretizedBoundedCompositionOptimizationStrategyTest {

  private final DiscretizedBoundedCompositionOptimizationStrategy strategy =
      new DiscretizedBoundedCompositionOptimizationStrategy();
  private final MassBalanceCompositionCalculator calculator =
      new MassBalanceCompositionCalculator();

  @Test
  void ranksDiscreteCompositionsByDistanceToTarget() {
    final var result = strategy.optimize(new CompositionOptimizationRequest(
        List.of(
            source("low-acid", "1.060", "3.0", null),
            source("sharp", "1.040", "9.0", null),
            source("middle", "1.055", "5.2", null)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        TargetAreaProfileTargetObjectiveFunction.euclidean(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ), calculator);

    assertThat(result)
        .returns(3, CompositionOptimizationResult::evaluatedCandidates)
        .satisfies(value -> assertThat(value.candidates())
            .hasSize(3)
            .first()
            .satisfies(candidate -> assertThat(candidate.composition().sources().stream()
                .map(CompositionSource::weight)
                .map(CompositionProportion.class::cast)
                .map(CompositionProportion::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(BigDecimal.ONE))
            .satisfies(candidate -> assertThat(candidate.distance())
                .returns(true, ProfileTargetDistance::insideTarget)
                .satisfies(distance -> assertThat(distance.value())
                    .isEqualByComparingTo(BigDecimal.ZERO))));
    final var candidates = result.candidates();
    assertThatThrownBy(() -> new CompositionOptimizationResult(0, candidates))
        .isInstanceOf(InvalidCompositionOptimizationException.class)
        .hasMessage("returned composition candidates cannot exceed evaluated candidate count");
  }

  @Test
  void skipsAbsoluteVolumeCandidatesThatExceedAvailableVolume() {
    final var result = strategy.optimize(new CompositionOptimizationRequest(
        List.of(
            source("limited", "1.060", "3.0", VolumeValue.ofLiters("10")),
            source("sharp", "1.040", "9.0", VolumeValue.ofLiters("100"))
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.045", "1.055", "6.0", "8.0"),
        TargetAreaProfileTargetObjectiveFunction.euclidean(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 10),
        CompositionOptimizationMode.ABSOLUTE_VOLUME,
        VolumeValue.ofLiters("100")
    ), calculator);

    assertThat(result)
        .returns(0, CompositionOptimizationResult::evaluatedCandidates)
        .satisfies(value -> assertThat(value.candidates()).isEmpty());
  }

  @Test
  void ranksByObjectiveScoreWithYanReadinessPenalty() {
    final var result = strategy.optimize(new CompositionOptimizationRequest(
        List.of(
            source("low-yan-a", "1.050", "6.0", "60", null),
            source("low-yan-b", "1.050", "6.0", "60", null),
            source("ready-a", "1.052", "6.2", "200", null),
            source("ready-b", "1.052", "6.2", "200", null)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        TargetAreaProfileTargetObjectiveFunction.euclidean(
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

    assertThat(result.candidates())
        .isNotEmpty()
        .first()
        .satisfies(candidate -> assertThat(candidate.objectiveAssessment().penalties()).isEmpty())
        .satisfies(candidate -> assertThat(candidate.distance().value())
            .isNotNull()
            .isGreaterThan(BigDecimal.ZERO));
  }

  @Test
  void excludesCandidatesWithMissingYanWhenPolicyRequiresIt() {
    final var result = strategy.optimize(new CompositionOptimizationRequest(
        List.of(
            source("without-yan", "1.050", "6.0", null),
            source("with-yan", "1.052", "6.2", "200", null)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        TargetAreaProfileTargetObjectiveFunction.euclidean(
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

    assertThat(result)
        .returns(1, CompositionOptimizationResult::evaluatedCandidates)
        .satisfies(value -> assertThat(value.candidates()).isEmpty());
  }

  @Test
  void rejectsDuplicateSourceReferencesBeforeOptimization() {
    assertThatThrownBy(() -> new CompositionOptimizationRequest(
        List.of(
            source("same", "1.050", "6.0", null),
            source("same", "1.052", "6.2", null)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target("1.049", "1.051", "5.9", "6.1"),
        TargetAreaProfileTargetObjectiveFunction.euclidean(
            EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults(),
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ))
        .isInstanceOf(InvalidCompositionOptimizationException.class)
        .hasMessageContaining("each source exactly once");
  }

  private static @NonNull ProfileTargetObjective target(
      @NonNull String specificGravityMin,
      @NonNull String specificGravityMax,
      @NonNull String titratableAcidityMin,
      @NonNull String titratableAcidityMax
  ) {
    return ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            closed(
                SpecificGravityValue.of(specificGravityMin),
                SpecificGravityValue.of(specificGravityMax)
            ),
            closed(
                TitratableAcidityValue.of(titratableAcidityMin),
                TitratableAcidityValue.of(titratableAcidityMax)
            )
        )
    ));
  }

  private static @NonNull CompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable VolumeValue availableVolume
  ) {
    return source(id, specificGravity, titratableAcidity, null, availableVolume);
  }

  private static @NonNull CompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String yeastAssimilableNitrogen,
      @Nullable VolumeValue availableVolume
  ) {
    return new CompositionOptimizationSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics(
            specificGravity,
            titratableAcidity,
            null,
            yeastAssimilableNitrogen
        ),
        availableVolume
    );
  }

}
