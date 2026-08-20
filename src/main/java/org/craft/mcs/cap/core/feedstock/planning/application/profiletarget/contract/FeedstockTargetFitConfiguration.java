package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Feedstock planning choices that determine target-fit scoring semantics. */
public record FeedstockTargetFitConfiguration(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull ObjectiveWeights weights
) {

  public FeedstockTargetFitConfiguration {
    requireNonNull(analysisScenario, "feedstock target analysis scenario cannot be null");
    requireNonNull(weights, "feedstock target weights cannot be null");
  }

  public @NonNull Normalizer normalizer() {
    return switch (analysisScenario) {
      case GRAPH_EXPLORATION -> Normalizer.MIN_MAX;
      case BLENDING_PLANNING -> Normalizer.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> Normalizer.THRESHOLD_SEGMENT;
    };
  }

  public record ObjectiveWeights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    public ObjectiveWeights {
      requireNonNull(specificGravity, "specific gravity weight cannot be null");
      requireNonNull(titratableAcidity, "titratable acidity weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("feedstock target weights must be positive");
      }
    }

    public static @NonNull ObjectiveWeights balanced() {
      return new ObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE, null);
    }

    public @NonNull BigDecimal tanninOrDefault() {
      return requireNonNullElse(tannin, BigDecimal.ONE); // NOSONAR
    }
  }

  public enum AnalysisScenario {
    GRAPH_EXPLORATION,
    BLENDING_PLANNING,
    TARGET_ZONE_MATCHING
  }

  public enum Normalizer {
    MIN_MAX,
    EFFECTIVE_RANGE,
    THRESHOLD_SEGMENT,
    SENSITIVITY_UNIT
  }

  public enum ObjectiveFunction {
    DISTANCE_TO_TARGET_AREA,
    MANHATTAN_DISTANCE_TO_TARGET_AREA
  }
}
