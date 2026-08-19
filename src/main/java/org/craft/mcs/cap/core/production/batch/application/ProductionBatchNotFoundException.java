package org.craft.mcs.cap.core.production.batch.application;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;


@SuppressWarnings("java:S110")
public final class ProductionBatchNotFoundException extends ApplicationNotFoundException {

  public ProductionBatchNotFoundException(@NonNull ProductionBatchId batchId) {
    super("production batch not found: " + batchId.value());
  }
}
