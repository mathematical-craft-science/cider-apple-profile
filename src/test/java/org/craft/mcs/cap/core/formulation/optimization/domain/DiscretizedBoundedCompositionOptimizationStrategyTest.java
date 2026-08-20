package org.craft.mcs.cap.core.formulation.optimization.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.domain.MassBalanceCompositionCalculator;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.DiscretizedBoundedCompositionOptimizationStrategy;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures
    .compositionSourceCharacteristics;


class DiscretizedBoundedCompositionOptimizationStrategyTest {

  private final DiscretizedBoundedCompositionOptimizationStrategy strategy =
      new DiscretizedBoundedCompositionOptimizationStrategy();
  private final MassBalanceCompositionCalculator calculator =
      new MassBalanceCompositionCalculator();

  @Test
  void enumeratesFeasibleDiscreteCompositionsAndPreservesProportions() {
    final var result = strategy.optimize(new CompositionOptimizationRequest(
        List.of(
            source("low-acid", "1.060", "3.0", null),
            source("sharp", "1.040", "9.0", null),
            source("middle", "1.055", "5.2", null)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
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
                .isEqualByComparingTo(BigDecimal.ONE)));
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
        new DiscretizedCompositionOptimizationSettings(50, 10),
        CompositionOptimizationMode.ABSOLUTE_VOLUME,
        VolumeValue.ofLiters("100")
    ), calculator);

    assertThat(result)
        .returns(0, CompositionOptimizationResult::evaluatedCandidates)
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
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        null
    ))
        .isInstanceOf(InvalidCompositionOptimizationException.class)
        .hasMessageContaining("each source exactly once");
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
