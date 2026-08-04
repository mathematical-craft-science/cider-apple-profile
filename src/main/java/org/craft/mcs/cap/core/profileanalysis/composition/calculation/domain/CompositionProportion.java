package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Relative source weight expressed as a normalized proportion in the range {@code (0, 1]}.
 *
 * <p>Proportion mode is useful for exploratory planning when the user wants a blend profile without
 * committing to a concrete total volume.
 */
public record CompositionProportion(@NonNull BigDecimal value) implements CompositionSourceWeight {

  public CompositionProportion {
    requireNonNull(value, "composition proportion cannot be null");

    if (value.signum() <= 0 || value.compareTo(BigDecimal.ONE) > 0) {
      throw new InvalidCompositionException(
          "composition proportion must be greater than zero and at most one"
      );
    }
  }

  public static @NonNull CompositionProportion of(@NonNull String value) {
    return new CompositionProportion(new BigDecimal(value));
  }
}
