package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published criterion specifications that can affect candidate ranking without changing target fit. */
public sealed interface OptimizationCriterionInput permits OptimizationCriterionInput.YanReadiness {

  record YanReadiness(
      @Nullable YeastAssimilableNitrogenValue minimum,
      @Nullable YeastAssimilableNitrogenValue maximum,
      @NonNull BigDecimal weight,
      @NonNull MissingPolicy missingPolicy,
      @NonNull BigDecimal missingPenalty
  ) implements OptimizationCriterionInput {

    public YanReadiness {
      requireNonNull(weight, "optimization YAN weight cannot be null");
      requireNonNull(missingPolicy, "optimization YAN missing policy cannot be null");
      requireNonNull(missingPenalty, "optimization YAN missing penalty cannot be null");
    }
  }

  enum MissingPolicy { IGNORE_IF_MISSING, PENALIZE_IF_MISSING, EXCLUDE_IF_MISSING }
}
