package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Signals that an accepted production source reference cannot be resolved by its owning context.
 */
@SuppressWarnings("java:S110")
public final class ProductionSourceNotFoundException
    extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;

  public ProductionSourceNotFoundException(@NonNull ProductionSourceReference sourceReference) {
    super("production source not found: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }
}
