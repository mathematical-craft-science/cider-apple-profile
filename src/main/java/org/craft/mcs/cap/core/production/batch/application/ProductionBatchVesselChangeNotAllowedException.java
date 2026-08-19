package org.craft.mcs.cap.core.production.batch.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that initial vessel assignment can no longer be corrected after production operations. */
@SuppressWarnings("java:S110")
public final class ProductionBatchVesselChangeNotAllowedException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ProductionBatchVesselChangeNotAllowedException(@NonNull ProductionBatchId batchId) {
    super("production batch vessel cannot be changed after the first operation: "
        + requireNonNull(batchId, "production batch id cannot be null").value());
  }
}
