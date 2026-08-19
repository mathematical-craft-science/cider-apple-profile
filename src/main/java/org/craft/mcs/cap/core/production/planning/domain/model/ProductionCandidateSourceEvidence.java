package org.craft.mcs.cap.core.production.planning.domain.model;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable evidence snapshot for one source assessed by a saved production candidate. */
@ValueObject
public record ProductionCandidateSourceEvidence(
    @NonNull ProductionSourceEvidenceReference evidenceReference,
    @NonNull MaterialStateId materialStateId
) {

  public ProductionCandidateSourceEvidence {
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
    requireNonNull(materialStateId, "production source material state id cannot be null");
  }
}
