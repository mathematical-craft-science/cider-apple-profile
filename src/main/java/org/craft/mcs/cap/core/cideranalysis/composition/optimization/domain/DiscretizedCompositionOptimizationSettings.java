package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import org.jspecify.annotations.NonNull;


/**
 * Parameters for the discrete bounded optimization algorithm.
 *
 * <p>{@code stepPercent} controls grid granularity. With step 5 the optimizer enumerates 0, 5, 10
 * ... 100 percent shares for each source, constrained so all shares sum to 100. Smaller steps are
 * more precise but grow the search grid combinatorially. {@code limit} controls how many best
 * candidates are returned after sorting by target distance.
 */
public record DiscretizedCompositionOptimizationSettings(
    int stepPercent,
    int limit
) {

  public static final int DEFAULT_STEP_PERCENT = 5;
  public static final int DEFAULT_LIMIT = 10;
  public static final int MAX_LIMIT = 100;
  public static final int MAX_SOURCE_COUNT = 8;
  public static final long MAX_GRID_COMBINATIONS = 200_000;

  public DiscretizedCompositionOptimizationSettings {
    if (stepPercent < 1 || stepPercent > 50) {
      throw new InvalidChemicalProfileCompositionOptimizationException("composition optimization step percent must be between 1 and 50");
    }
    if (100 % stepPercent != 0) {
      throw new InvalidChemicalProfileCompositionOptimizationException("composition optimization step percent must divide 100 exactly");
    }
    if (limit < 1 || limit > MAX_LIMIT) {
      throw new InvalidChemicalProfileCompositionOptimizationException("composition optimization limit must be between 1 and " + MAX_LIMIT);
    }
  }

  public static @NonNull DiscretizedCompositionOptimizationSettings defaults() {
    return new DiscretizedCompositionOptimizationSettings(DEFAULT_STEP_PERCENT, DEFAULT_LIMIT);
  }
}
