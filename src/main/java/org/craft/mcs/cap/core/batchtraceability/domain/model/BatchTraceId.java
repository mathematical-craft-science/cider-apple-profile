package org.craft.mcs.cap.core.batchtraceability.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@ValueObject
public record BatchTraceId(@NonNull UUID value) {

  public BatchTraceId {
    requireNonNull(value, "batch trace id cannot be null");
  }

  public static @NonNull BatchTraceId newId() {
    return new BatchTraceId(newUuid());
  }

  public static @NonNull BatchTraceId of(@NonNull UUID value) {
    return new BatchTraceId(value);
  }
}
