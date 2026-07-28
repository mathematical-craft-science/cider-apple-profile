package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Normalized absolute deltas that a distance metric aggregates without knowing target geometry. */
record NormalizedProfileDistanceVector(
    @NonNull BigDecimal specificGravity,
    @NonNull BigDecimal titratableAcidity,
    @NonNull BigDecimal tannin
) {

  NormalizedProfileDistanceVector {
    requireNonNull(specificGravity, "normalized specific gravity delta cannot be null");
    requireNonNull(titratableAcidity, "normalized titratable acidity delta cannot be null");
    requireNonNull(tannin, "normalized tannin delta cannot be null");
  }
}
