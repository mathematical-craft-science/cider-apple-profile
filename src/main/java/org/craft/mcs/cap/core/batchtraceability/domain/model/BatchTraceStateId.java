package org.craft.mcs.cap.core.batchtraceability.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Identifies one immutable historical state of a traceability subject. */
@ValueObject
public record BatchTraceStateId(@NonNull UUID value) {

  public BatchTraceStateId {
    requireNonNull(value, "batch trace state id cannot be null");
  }

  public static @NonNull BatchTraceStateId newId() {
    return new BatchTraceStateId(newUuid());
  }

  public static @NonNull BatchTraceStateId of(@NonNull UUID value) {
    return new BatchTraceStateId(value);
  }
}
