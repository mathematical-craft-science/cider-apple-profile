package org.craft.mcs.cap.core.production.candidate.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when source evidence changed after the production candidate was assessed. */
@SuppressWarnings("java:S110")
public final class ProductionBatchCandidateStaleException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionBatchCandidateStaleException(@NonNull ProductionSourceReference sourceReference) {
    super("production batch candidate is stale because source evidence changed");
    this.sourceReference = requireNonNull(sourceReference, "production source reference cannot be null");
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
