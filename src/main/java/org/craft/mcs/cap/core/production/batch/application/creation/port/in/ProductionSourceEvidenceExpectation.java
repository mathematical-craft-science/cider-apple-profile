package org.craft.mcs.cap.core.production.batch.application.creation.port.in;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Expected source evidence used to reject a stale accepted production plan. */
public record ProductionSourceEvidenceExpectation(
    @NonNull ProductionSourceEvidenceReference evidenceReference,
    @NonNull MaterialStateId materialStateId
) {

  public ProductionSourceEvidenceExpectation {
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
    requireNonNull(materialStateId, "production source material state id cannot be null");
  }
}
