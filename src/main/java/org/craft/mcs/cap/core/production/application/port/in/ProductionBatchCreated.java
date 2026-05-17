package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProductionBatchCreated(
    @NonNull ProductionBatch productionBatch,
    @NonNull ProductionInput input
) {

  public ProductionBatchCreated {
    requireNonNull(productionBatch, "production batch cannot be null");
    requireNonNull(input, "production input cannot be null");
  }
}
