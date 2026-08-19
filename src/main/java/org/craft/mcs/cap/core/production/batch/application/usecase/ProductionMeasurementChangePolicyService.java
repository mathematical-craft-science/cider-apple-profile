package org.craft.mcs.cap.core.production.batch.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.port.in.ProductionMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/** Applies the Production-owned lifecycle policy under a shared lock on the measured batch. */
@Component
@RequiredArgsConstructor
public class ProductionMeasurementChangePolicyService
    implements ProductionMeasurementChangePolicyUseCase {

  private final @NonNull ProductionBatchRepository repository;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void requireChangesAllowed(@NonNull ProductionBatchId batchId) {
    repository.lockForMeasurementChange(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId))
        .requireMeasurementChangesAllowed();
  }
}
