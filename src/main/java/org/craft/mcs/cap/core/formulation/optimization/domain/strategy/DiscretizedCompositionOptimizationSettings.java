package org.craft.mcs.cap.core.formulation.optimization.domain.strategy;

import org.craft.mcs.cap.core.formulation.optimization.domain.InvalidCompositionOptimizationException;
/**
 * Parameters for the discrete bounded optimization algorithm.
 *
 * <p>{@code stepPercent} controls grid granularity. With step 5 the optimizer enumerates 0, 5, 10
 * ... 100 percent shares for each source, constrained so all shares sum to 100. Smaller steps are
 * more precise but grow the search grid combinatorially. {@code limit} controls how many highest-ranked
 * candidates are returned after sorting by the complete optimization objective.
 */
public record DiscretizedCompositionOptimizationSettings(
    int stepPercent,
    int limit
) {

  public static final int MAX_LIMIT = 100;
  public static final int MAX_SOURCE_COUNT = 8;
  public static final long MAX_GRID_COMBINATIONS = 200_000;

  public DiscretizedCompositionOptimizationSettings {
    if (stepPercent < 1 || stepPercent > 50) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization step percent must be between 1 and 50"
      );
    }
    if (100 % stepPercent != 0) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization step percent must divide 100 exactly"
      );
    }
    if (limit < 1 || limit > MAX_LIMIT) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization limit must be between 1 and " + MAX_LIMIT
      );
    }
  }
}
