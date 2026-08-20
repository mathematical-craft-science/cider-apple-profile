package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Apple analysis choices that determine target-fit scoring semantics. */
public record AppleTargetFitConfiguration(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull ObjectiveWeights weights
) {

  public AppleTargetFitConfiguration {
    requireNonNull(analysisScenario, "apple target analysis scenario cannot be null");
    requireNonNull(weights, "apple target weights cannot be null");
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
      requireNonNull(specificGravity, "apple target specific gravity weight cannot be null");
      requireNonNull(titratableAcidity, "apple target titratable acidity weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("apple target weights must be positive");
      }
    }

    public static @NonNull ObjectiveWeights balanced() {
      return new ObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE, null);
    }
  }

  public enum AnalysisScenario { GRAPH_EXPLORATION, BLENDING_PLANNING, TARGET_ZONE_MATCHING }
  public enum Normalizer { MIN_MAX, EFFECTIVE_RANGE, THRESHOLD_SEGMENT, SENSITIVITY_UNIT }
}
