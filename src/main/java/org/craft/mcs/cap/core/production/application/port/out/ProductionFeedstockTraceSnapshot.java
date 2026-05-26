package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Current material snapshot used when production records traceability for a feedstock source.
 *
 * <p>The snapshot captures the feedstock chemical profile at the moment a production batch is
 * confirmed. This keeps future edits to measurements from changing the historical production
 * origin.
 */
public record ProductionFeedstockTraceSnapshot(
    @NonNull ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence
) {

  public ProductionFeedstockTraceSnapshot {
    requireNonNull(profile, "production feedstock trace profile cannot be null");
  }
}
