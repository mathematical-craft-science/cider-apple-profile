package org.craft.mcs.cap.core.cideranalysis.abv.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class StandardGravityDropAbvPredictionStrategy
    implements AbvPredictionStrategy {

  private static final BigDecimal ABV_COEFFICIENT = new BigDecimal("131.25");

  @Override
  public @NonNull AbvPredictionMethod method() {
    return AbvPredictionMethod.STANDARD_GRAVITY_DROP;
  }

  @Override
  public @NonNull AbvValue predict(@NonNull AbvPredictionInput input) {
    requireNonNull(input, "ABV prediction input cannot be null");

    return AbvValue.of(input.originalGravity().asBigDecimal()
        .subtract(input.finalGravity().asBigDecimal())
        .multiply(ABV_COEFFICIENT));
  }
}
