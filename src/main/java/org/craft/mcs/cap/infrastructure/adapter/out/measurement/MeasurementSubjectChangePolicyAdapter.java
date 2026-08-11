package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectChangePolicy;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.production.application.port.in.ProductionMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL adapter that delegates measurement-change eligibility to the subject's owning context. */
@Component
@RequiredArgsConstructor
final class MeasurementSubjectChangePolicyAdapter implements MeasurementSubjectChangePolicy {

  private final @NonNull FeedstockMeasurementChangePolicyUseCase feedstockPolicy;
  private final @NonNull ProductionMeasurementChangePolicyUseCase productionPolicy;

  @Override
  public void requireChangesAllowed(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    switch (subject.type()) {
      case FEEDSTOCK_BATCH -> feedstockPolicy.requireChangesAllowed(FeedstockBatchId.of(subject.id()));
      case PRODUCTION_BATCH -> productionPolicy.requireChangesAllowed(ProductionBatchId.of(subject.id()));
    }
  }
}
