package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/**
 * Full optimization objective result for one candidate.
 *
 * <p>{@code score} is the value used to rank candidates. It is intentionally separate from
 * profile-target distance: distance remains a pure target-fit explanation, while score may include
 * production-readiness penalties such as low or missing YAN.
 */
public record CompositionOptimizationObjectiveAssessment(
    @Nullable BigDecimal score,
    @NonNull List<@NonNull CompositionOptimizationPenalty> penalties
) {

  public CompositionOptimizationObjectiveAssessment {
    penalties = List.copyOf(requireNonNull(penalties, "composition optimization penalties cannot be null"));
    if (score != null && isNegative(score)) {
      throw new InvalidChemicalProfileCompositionOptimizationException("composition optimization score cannot be negative");
    }
  }

  public boolean scored() {
    return score != null;
  }
}
