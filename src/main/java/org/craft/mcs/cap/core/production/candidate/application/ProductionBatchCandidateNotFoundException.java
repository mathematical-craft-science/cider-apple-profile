package org.craft.mcs.cap.core.production.candidate.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that a requested saved production candidate does not exist. */
@SuppressWarnings("java:S110")
public final class ProductionBatchCandidateNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ProductionBatchCandidateNotFoundException(@NonNull ProductionBatchCandidateId candidateId) {
    super("production batch candidate not found: "
        + requireNonNull(candidateId, "candidate id cannot be null").value());
  }
}
