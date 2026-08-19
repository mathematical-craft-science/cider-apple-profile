package org.craft.mcs.cap.core.production.batch.application.creation;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Indicates that source material no longer matches the accepted creation input. */
@SuppressWarnings("java:S110")
public final class ProductionSourceEvidenceConflictException extends ApplicationConflictException {

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionSourceEvidenceConflictException(@NonNull ProductionSourceReference sourceReference) {
    super("production source evidence changed before batch creation");
    this.sourceReference = requireNonNull(sourceReference, "production source reference cannot be null");
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
