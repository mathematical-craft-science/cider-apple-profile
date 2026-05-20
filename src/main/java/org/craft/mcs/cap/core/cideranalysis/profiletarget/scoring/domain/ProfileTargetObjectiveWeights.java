package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Per-axis weights used by objective functions.
 *
 * <p>Weights are applied after axis normalization. A higher weight means deviations on that axis are
 * more important for ranking candidates. Tannin is nullable because tannin can be omitted from the
 * objective entirely; when tannin target is present and weight is omitted, objective functions use
 * {@code 1}.
 */
public record ProfileTargetObjectiveWeights(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity,
    @Nullable BigDecimal tannin
) {

  public ProfileTargetObjectiveWeights(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  ) {
    this(sugar, acidity, null);
  }

  public ProfileTargetObjectiveWeights {
    requireNonNull(sugar, "sugar objective weight cannot be null");
    requireNonNull(acidity, "acidity objective weight cannot be null");
    if (sugar.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainRuleViolationException("sugar objective weight must be positive");
    }
    if (acidity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainRuleViolationException("acidity objective weight must be positive");
    }
    if (tannin != null && tannin.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainRuleViolationException("tannin objective weight must be positive");
    }
  }

  public static @NonNull ProfileTargetObjectiveWeights balanced() {
    return new ProfileTargetObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE);
  }

  public @NonNull BigDecimal tanninOrDefault() {
    return tannin == null ? BigDecimal.ONE : tannin;
  }
}
