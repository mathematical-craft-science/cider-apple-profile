package org.craft.mcs.cap.core.measurement.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Identity of an immutable direct, observed, or derived measurement fact. */
@ValueObject
public record MeasurementRecordId(@NonNull UUID value) {

  public MeasurementRecordId {
    requireNonNull(value, "measurement record id cannot be null");
  }

  public static @NonNull MeasurementRecordId newId() {
    return new MeasurementRecordId(newUuid());
  }

  public static @NonNull MeasurementRecordId of(@NonNull UUID value) {
    return new MeasurementRecordId(value);
  }
}
