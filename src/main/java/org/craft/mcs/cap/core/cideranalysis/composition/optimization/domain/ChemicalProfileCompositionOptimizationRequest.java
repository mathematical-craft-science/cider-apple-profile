package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunction;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Full domain request for optimizing source composition against a target profile.
 *
 * <p>The request is intentionally explicit: it contains sources, tannin policy, objective function,
 * optimizer settings and weight mode. It does not contain algorithm or calculator codes because
 * those are resolved by the application layer before a concrete strategy is invoked.
 *
 * <p>Invariants enforced here protect the expensive optimization path:
 * at least two sources are required, source count and grid size are bounded, and volume semantics
 * are validated according to {@link CompositionOptimizationMode}.
 */
public record ChemicalProfileCompositionOptimizationRequest(
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationSource> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @NonNull ProfileTargetObjective targetObjective,
    @NonNull ProfileTargetObjectiveFunction objectiveFunction,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @Nullable VolumeValue targetTotalVolume
) {

  public ChemicalProfileCompositionOptimizationRequest {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");
    requireNonNull(targetObjective, "profile target objective cannot be null");
    requireNonNull(objectiveFunction, "profile target objective function cannot be null");
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");

    if (sources.size() < 2) {
      throw new IllegalArgumentException("chemical profile composition optimization requires at least two sources");
    }
    if (sources.size() > DiscretizedCompositionOptimizationSettings.MAX_SOURCE_COUNT) {
      throw new IllegalArgumentException(
          "chemical profile composition optimization supports at most "
              + DiscretizedCompositionOptimizationSettings.MAX_SOURCE_COUNT
              + " sources"
      );
    }
    final var gridCombinations = gridCombinations(sources.size(), settings.stepPercent());
    if (gridCombinations > DiscretizedCompositionOptimizationSettings.MAX_GRID_COMBINATIONS) {
      throw new IllegalArgumentException(
          "chemical profile composition optimization grid is too large: "
              + gridCombinations
              + " combinations"
      );
    }
    if (mode == CompositionOptimizationMode.ABSOLUTE_VOLUME && targetTotalVolume == null) {
      throw new IllegalArgumentException("absolute volume composition optimization requires target total volume");
    }
    if (mode == CompositionOptimizationMode.ABSOLUTE_VOLUME && targetTotalVolume.isZero()) { // NOSONAR
      throw new IllegalArgumentException("absolute volume composition optimization requires target total volume greater than zero");
    }
    if (mode == CompositionOptimizationMode.PROPORTION && targetTotalVolume != null) {
      throw new IllegalArgumentException("proportion composition optimization must not define target total volume");
    }
  }

  private static long gridCombinations(int sourceCount, int stepPercent) {
    final var units = 100 / stepPercent;
    final var n = units + sourceCount - 1;
    final var k = sourceCount - 1;

    var result = 1L;
    for (var index = 1; index <= k; index++) {
      result = Math.multiplyExact(result, n - k + index) / index;
      if (result > DiscretizedCompositionOptimizationSettings.MAX_GRID_COMBINATIONS) {
        return result;
      }
    }
    return result;
  }
}
