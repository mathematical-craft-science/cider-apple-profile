package org.craft.mcs.cap.core.formulation.optimization.domain;

import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;


/**
 * Full domain request for optimizing source composition against a target profile.
 *
 * <p>The request contains only bounded-search inputs. Target scoring, readiness criteria and
 * ranking are application orchestration concerns evaluated after candidate generation.
 *
 * <p>Invariants enforced here protect the expensive optimization path:
 * at least two sources are required, source count and grid size are bounded, and volume semantics
 * are validated according to {@link CompositionOptimizationMode}.
 */
public record CompositionOptimizationRequest(
    @NonNull List<@NonNull CompositionOptimizationSource> sources,
    @NonNull CompositionTanninPolicy tanninPolicy,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @Nullable VolumeValue targetTotalVolume
) {

  public CompositionOptimizationRequest {
    sources = List.copyOf(requireNonNull(sources, "composition optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");

    if (sources.size() < 2) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization requires at least two sources"
      );
    }
    if (sources.size() > DiscretizedCompositionOptimizationSettings.MAX_SOURCE_COUNT) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization supports at most "
              + DiscretizedCompositionOptimizationSettings.MAX_SOURCE_COUNT
              + " sources"
      );
    }
    if (sources.stream()
        .map(CompositionOptimizationSource::reference)
        .distinct()
        .count() != sources.size()) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization must contain each source exactly once"
      );
    }
    final var gridCombinations = gridCombinations(sources.size(), settings.stepPercent());
    if (gridCombinations > DiscretizedCompositionOptimizationSettings.MAX_GRID_COMBINATIONS) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization grid is too large: "
              + gridCombinations
              + " combinations"
      );
    }
    if (mode == CompositionOptimizationMode.ABSOLUTE_VOLUME) {
      validateAbsoluteVolumeTarget(targetTotalVolume);
    } else if (targetTotalVolume != null) {
      throw new InvalidCompositionOptimizationException(
          "proportion composition optimization must not define target total volume"
      );
    }
  }

  private static void validateAbsoluteVolumeTarget(@Nullable VolumeValue targetTotalVolume) {
    if (targetTotalVolume == null) {
      throw new InvalidCompositionOptimizationException(
          "absolute volume composition optimization requires target total volume"
      );
    }
    if (targetTotalVolume.isZero()) {
      throw new InvalidCompositionOptimizationException(
          "absolute volume composition optimization requires target total volume greater than zero"
      );
    }
  }

  private static long gridCombinations(
      int sourceCount,
      int stepPercent
  ) {
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
