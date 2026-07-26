package org.craft.mcs.cap.core.traceability.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@ValueObject
public record BatchTraceStepId(@NonNull UUID value) {

  public BatchTraceStepId {
    requireNonNull(value, "batch trace step id cannot be null");
  }

  public static @NonNull BatchTraceStepId newId() {
    return new BatchTraceStepId(newUuid());
  }

  public static @NonNull BatchTraceStepId of(@NonNull UUID value) {
    return new BatchTraceStepId(value);
  }
}
