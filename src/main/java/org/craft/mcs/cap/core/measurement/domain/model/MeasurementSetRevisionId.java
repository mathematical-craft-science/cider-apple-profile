package org.craft.mcs.cap.core.measurement.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Identity of one immutable revision of a measurement set. */
@ValueObject
public record MeasurementSetRevisionId(@NonNull UUID value) {

  public MeasurementSetRevisionId {
    requireNonNull(value, "measurement set revision id cannot be null");
  }

  public static @NonNull MeasurementSetRevisionId newId() {
    return new MeasurementSetRevisionId(newUuid());
  }

  public static @NonNull MeasurementSetRevisionId of(@NonNull UUID value) {
    return new MeasurementSetRevisionId(value);
  }
}
