package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


/** Bounded search resolution and result limit for one optimization request. */
public record CompositionOptimizationSettings(int stepPercent, int limit) {

  public CompositionOptimizationSettings {
    if (stepPercent < 1 || stepPercent > 50 || 100 % stepPercent != 0) {
      throw new ApplicationValidationException(
          "optimization step percent must be between 1 and 50 and divide 100 exactly"
      );
    }
    if (limit < 1 || limit > 100) {
      throw new ApplicationValidationException("optimization candidate limit must be between 1 and 100");
    }
  }

  public static @NonNull CompositionOptimizationSettings defaults() {
    return new CompositionOptimizationSettings(5, 10);
  }
}
