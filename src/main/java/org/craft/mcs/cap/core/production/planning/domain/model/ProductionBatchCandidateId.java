package org.craft.mcs.cap.core.production.planning.domain.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Identity of a saved production candidate assessment. */
public record ProductionBatchCandidateId(@NonNull UUID value) {

  public ProductionBatchCandidateId {
    requireNonNull(value, "production batch candidate id cannot be null");
  }

  public static @NonNull ProductionBatchCandidateId newId() {
    return new ProductionBatchCandidateId(newUuid());
  }

  public static @NonNull ProductionBatchCandidateId of(@NonNull UUID value) {
    return new ProductionBatchCandidateId(value);
  }
}
