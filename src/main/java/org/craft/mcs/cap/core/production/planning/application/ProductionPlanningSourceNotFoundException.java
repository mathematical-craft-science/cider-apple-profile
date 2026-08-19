package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Indicates that a source selected for production planning no longer exists. */
@SuppressWarnings("java:S110")
public final class ProductionPlanningSourceNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionPlanningSourceNotFoundException(
      @NonNull ProductionSourceReference sourceReference
  ) {
    super("production source not found: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
