package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchAccessUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchAccessUseCase;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL adapter that resolves generic subjects through inbound ports of their owning contexts. */
@Component
@RequiredArgsConstructor
final class MeasurementSubjectAccessControlAdapter implements MeasurementSubjectAccessControl {

  private final @NonNull FeedstockBatchAccessUseCase feedstockBatchAccessUseCase;
  private final @NonNull ProductionBatchAccessUseCase productionBatchAccessUseCase;

  @Override
  public @NonNull WorkspaceId requireReadAccess(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    return switch (subject.type()) {
      case FEEDSTOCK_BATCH -> feedstockBatchAccessUseCase.requireReadAccess(
          FeedstockBatchId.of(subject.id())
      );
      case PRODUCTION_BATCH -> productionBatchAccessUseCase.requireReadAccess(
          ProductionBatchId.of(subject.id())
      );
    };
  }

  @Override
  public void requireReadAccess(
      @NonNull MeasurementSubjectType subjectType,
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(subjectType, "measurement subject type cannot be null");
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    switch (subjectType) {
      case FEEDSTOCK_BATCH -> feedstockBatchAccessUseCase.requireWorkspaceReadAccess(workspaceId);
      case PRODUCTION_BATCH -> productionBatchAccessUseCase.requireWorkspaceReadAccess(workspaceId);
    }
  }

  @Override
  public @NonNull WorkspaceId requireWriteAccess(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    return switch (subject.type()) {
      case FEEDSTOCK_BATCH -> feedstockBatchAccessUseCase.requireWriteAccess(
          FeedstockBatchId.of(subject.id())
      );
      case PRODUCTION_BATCH -> productionBatchAccessUseCase.requireWriteAccess(
          ProductionBatchId.of(subject.id())
      );
    };
  }
}
