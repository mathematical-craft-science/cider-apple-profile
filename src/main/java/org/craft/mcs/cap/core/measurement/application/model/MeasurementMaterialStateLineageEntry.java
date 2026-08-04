package org.craft.mcs.cap.core.measurement.application.model;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Measurement-owned view of one material state and characteristics invalidated while advancing
 * from that state to the current state.
 */
public record MeasurementMaterialStateLineageEntry(
    @NonNull MaterialStateId materialStateId,
    int depth,
    @NonNull Set<@NonNull MeasurementCharacteristic> invalidatedCharacteristics
) {

  public MeasurementMaterialStateLineageEntry {
    requireNonNull(materialStateId, "lineage material state id cannot be null");
    invalidatedCharacteristics = Set.copyOf(requireNonNull(
        invalidatedCharacteristics,
        "lineage invalidated characteristics cannot be null"
    ));
    if (depth < 0) {
      throw new ApplicationValidationException("lineage depth cannot be negative");
    }
  }
}
