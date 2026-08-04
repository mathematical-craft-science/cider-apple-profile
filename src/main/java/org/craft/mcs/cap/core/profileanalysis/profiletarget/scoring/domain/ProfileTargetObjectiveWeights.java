package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/**
 * Per-axis weights used by objective functions.
 *
 * <p>Weights are applied after axis normalization. A higher weight means deviations on that axis are
 * more important for ranking candidates. Tannin is nullable because tannin can be omitted from the
 * objective entirely; when tannin target is present and weight is omitted, objective functions use
 * {@code 1}.
 */
public record ProfileTargetObjectiveWeights(
    @NonNull BigDecimal specificGravity,
    @NonNull BigDecimal titratableAcidity,
    @Nullable BigDecimal tannin
) {

  public ProfileTargetObjectiveWeights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity
  ) {
    this(specificGravity, titratableAcidity, null);
  }

  public ProfileTargetObjectiveWeights {
    requireNonNull(specificGravity, "specific gravity objective weight cannot be null");
    requireNonNull(titratableAcidity, "titratable acidity objective weight cannot be null");
    if (!isPositive(specificGravity)) {
      throw new DomainRuleViolationException("specific gravity objective weight must be positive");
    }
    if (!isPositive(titratableAcidity)) {
      throw new DomainRuleViolationException("titratable acidity objective weight must be positive");
    }
    if (tannin != null && !isPositive(tannin)) {
      throw new DomainRuleViolationException("tannin objective weight must be positive");
    }
  }

  public static @NonNull ProfileTargetObjectiveWeights balanced() {
    return new ProfileTargetObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE);
  }

  public @NonNull BigDecimal tanninOrDefault() {
    return requireNonNullElse(tannin, BigDecimal.ONE); // NOSONAR
  }
}
