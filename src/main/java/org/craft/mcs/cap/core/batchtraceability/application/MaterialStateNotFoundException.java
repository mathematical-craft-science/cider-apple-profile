package org.craft.mcs.cap.core.batchtraceability.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Raised when an immutable material state cannot be resolved from trace history.
 */
@SuppressWarnings("java:S110")
public final class MaterialStateNotFoundException
    extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MaterialStateNotFoundException(@NonNull MaterialStateId materialStateId) {
    super("material state not found: " + requireNonNull(materialStateId, "material state id cannot be null").value());
  }
}
