package org.craft.mcs.cap.core.production.domain.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProductionBatchId(@NonNull UUID value) {

  public ProductionBatchId {
    requireNonNull(value, "production batch id cannot be null");
  }

  public static @NonNull ProductionBatchId newId() {
    return new ProductionBatchId(UUID.randomUUID());
  }

  public static @NonNull ProductionBatchId of(@NonNull UUID value) {
    return new ProductionBatchId(value);
  }
}
