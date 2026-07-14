package org.craft.mcs.cap.core.production.domain.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


public record ProductionBatchId(@NonNull UUID value) {

  public ProductionBatchId {
    requireNonNull(value, "production batch id cannot be null");
  }

  public static @NonNull ProductionBatchId newId() {
    return new ProductionBatchId(newUuid());
  }

  public static @NonNull ProductionBatchId of(@NonNull UUID value) {
    return new ProductionBatchId(value);
  }
}
