package org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/** Curated pH range shared by generic cider planning and fermentation-start readiness. */
public record CiderPhGuidanceRange(
    @NonNull BigDecimal minimum,
    @NonNull BigDecimal maximum
) {

  private static final @NonNull BigDecimal CURATED_CIDER_V1_MINIMUM = new BigDecimal("3.3");
  private static final @NonNull BigDecimal CURATED_CIDER_V1_MAXIMUM = new BigDecimal("3.8");

  public CiderPhGuidanceRange {
    requireNonNull(minimum, "cider pH guidance minimum cannot be null");
    requireNonNull(maximum, "cider pH guidance maximum cannot be null");
    if (isGreaterThan(minimum, maximum)) {
      throw new DomainRuleViolationException("cider pH guidance minimum must not exceed maximum");
    }
  }

  public static @NonNull CiderPhGuidanceRange curatedCiderV1() {
    return new CiderPhGuidanceRange(CURATED_CIDER_V1_MINIMUM, CURATED_CIDER_V1_MAXIMUM);
  }

  public boolean isBelow(@NonNull BigDecimal value) {
    return isLessThan(requireNonNull(value, "pH value cannot be null"), minimum);
  }

  public boolean isAbove(@NonNull BigDecimal value) {
    return isGreaterThan(requireNonNull(value, "pH value cannot be null"), maximum);
  }
}
