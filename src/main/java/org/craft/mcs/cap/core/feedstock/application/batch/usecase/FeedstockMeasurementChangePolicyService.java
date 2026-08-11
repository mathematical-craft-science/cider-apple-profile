package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/** Applies the Feedstock-owned lifecycle policy under a shared lock on the measured batch. */
@Component
@RequiredArgsConstructor
public class FeedstockMeasurementChangePolicyService
    implements FeedstockMeasurementChangePolicyUseCase {

  private final @NonNull FeedstockBatchRepository repository;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void requireChangesAllowed(@NonNull FeedstockBatchId batchId) {
    repository.lockForMeasurementChange(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId))
        .requireMeasurementChangesAllowed();
  }
}
