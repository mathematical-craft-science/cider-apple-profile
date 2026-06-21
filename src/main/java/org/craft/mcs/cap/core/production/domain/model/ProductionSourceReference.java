package org.craft.mcs.cap.core.production.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Production-owned reference to a material source accepted for production.
 *
 * <p>The production context stores only source type and source id. Source-specific validation,
 * volume accounting and snapshots stay in application ports/adapters and traceability.
 */
@ValueObject
public record ProductionSourceReference(
    @NonNull ProductionSourceType type,
    @NonNull UUID id
) {

  public ProductionSourceReference {
    requireNonNull(type, "production source type cannot be null");
    requireNonNull(id, "production source id cannot be null");
  }

  public static @NonNull ProductionSourceReference feedstockBatch(@NonNull UUID id) {
    return new ProductionSourceReference(ProductionSourceType.FEEDSTOCK_BATCH, id);
  }
}
