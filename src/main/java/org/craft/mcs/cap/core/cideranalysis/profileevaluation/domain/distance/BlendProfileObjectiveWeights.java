package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record BlendProfileObjectiveWeights(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity,
    @Nullable BigDecimal tannin
) {

  public BlendProfileObjectiveWeights(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  ) {
    this(sugar, acidity, null);
  }

  public BlendProfileObjectiveWeights {
    requireNonNull(sugar, "sugar objective weight cannot be null");
    requireNonNull(acidity, "acidity objective weight cannot be null");
    if (sugar.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("sugar objective weight must be positive");
    }
    if (acidity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("acidity objective weight must be positive");
    }
    if (tannin != null && tannin.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("tannin objective weight must be positive");
    }
  }

  public static @NonNull BlendProfileObjectiveWeights balanced() {
    return new BlendProfileObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE);
  }

  public @NonNull BigDecimal tanninOrDefault() {
    return tannin == null ? BigDecimal.ONE : tannin;
  }
}
