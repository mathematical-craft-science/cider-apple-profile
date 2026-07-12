package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Signals that a production source exists but cannot provide a current chemical snapshot.
 */
@SuppressWarnings("java:S110")
public final class ProductionSourceCurrentProfileUnavailableException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionSourceCurrentProfileUnavailableException(@NonNull ProductionSourceReference sourceReference) {
    super("production source current profile is unavailable: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
