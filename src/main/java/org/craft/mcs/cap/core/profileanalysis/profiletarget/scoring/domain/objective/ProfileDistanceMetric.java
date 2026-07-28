package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public enum ProfileDistanceMetric {

  /**
   * Euclidean distance after axis normalization and per-axis weighting.
   */
  NORMALIZED_WEIGHTED_EUCLIDEAN {
    @Override
    @NonNull BigDecimal aggregate(
        @NonNull NormalizedProfileDistanceVector vector,
        @NonNull ProfileTargetObjectiveWeights weights
    ) {
      return weightedSquare(vector.specificGravity(), weights.specificGravity())
          .add(weightedSquare(vector.titratableAcidity(), weights.titratableAcidity()), MATH_CONTEXT)
          .add(weightedSquare(vector.tannin(), weights.tanninOrDefault()), MATH_CONTEXT)
          .sqrt(MATH_CONTEXT)
          .stripTrailingZeros();
    }
  },

  /**
   * Manhattan distance after axis normalization and per-axis weighting.
   */
  NORMALIZED_WEIGHTED_MANHATTAN {
    @Override
    @NonNull BigDecimal aggregate(
        @NonNull NormalizedProfileDistanceVector vector,
        @NonNull ProfileTargetObjectiveWeights weights
    ) {
      return weighted(vector.specificGravity(), weights.specificGravity())
          .add(weighted(vector.titratableAcidity(), weights.titratableAcidity()), MATH_CONTEXT)
          .add(weighted(vector.tannin(), weights.tanninOrDefault()), MATH_CONTEXT)
          .stripTrailingZeros();
    }
  };

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  abstract @NonNull BigDecimal aggregate(
      @NonNull NormalizedProfileDistanceVector vector,
      @NonNull ProfileTargetObjectiveWeights weights
  );

  private static @NonNull BigDecimal weightedSquare(
      @NonNull BigDecimal delta,
      @NonNull BigDecimal weight
  ) {
    return requireNonNull(weight, "profile target axis weight cannot be null")
        .multiply(
            requireNonNull(delta, "normalized profile axis delta cannot be null")
                .multiply(delta, MATH_CONTEXT),
            MATH_CONTEXT
        );
  }

  private static @NonNull BigDecimal weighted(
      @NonNull BigDecimal delta,
      @NonNull BigDecimal weight
  ) {
    return requireNonNull(weight, "profile target axis weight cannot be null")
        .multiply(requireNonNull(delta, "normalized profile axis delta cannot be null"), MATH_CONTEXT);
  }
}
