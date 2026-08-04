package org.craft.mcs.cap.core.measurement.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Stable identity of a logical measurement set across all corrections. */
@ValueObject
public record MeasurementSetId(@NonNull UUID value) {

  public MeasurementSetId {
    requireNonNull(value, "measurement set id cannot be null");
  }

  public static @NonNull MeasurementSetId newId() {
    return new MeasurementSetId(newUuid());
  }

  public static @NonNull MeasurementSetId of(@NonNull UUID value) {
    return new MeasurementSetId(value);
  }
}
