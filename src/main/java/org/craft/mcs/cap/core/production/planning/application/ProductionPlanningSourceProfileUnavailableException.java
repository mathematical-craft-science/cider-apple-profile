package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Indicates that a source lacks the current characteristics required by production planning. */
@SuppressWarnings("java:S110")
public final class ProductionPlanningSourceProfileUnavailableException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionPlanningSourceProfileUnavailableException(
      @NonNull ProductionSourceReference sourceReference
  ) {
    super("production source has no current characteristics: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
