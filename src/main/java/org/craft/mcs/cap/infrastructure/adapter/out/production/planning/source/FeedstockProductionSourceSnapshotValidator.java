package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceExpectation;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ValidateFeedstockBatchMaterialSnapshotCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ValidateFeedstockBatchMaterialSnapshotUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceSnapshotValidator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source.FeedstockProductionSourceFailureTranslator.translate;


/** ACL for atomic Feedstock source-token validation before a production candidate is stored. */
@Component
@RequiredArgsConstructor
public final class FeedstockProductionSourceSnapshotValidator implements ProductionSourceSnapshotValidator {

  private final @NonNull ValidateFeedstockBatchMaterialSnapshotUseCase validateFeedstockSnapshotUseCase;

  @Override
  public void validate(
      @NonNull WorkspaceId workspaceId,
      @NonNull AvailableProductionSource source
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(source, "available production source cannot be null");
    switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> validateFeedstock(workspaceId, source);
    }
  }

  private void validateFeedstock(
      @NonNull WorkspaceId workspaceId,
      @NonNull AvailableProductionSource source
  ) {
    translate(source.reference(), () -> {
      validateFeedstockSnapshotUseCase.validate(new ValidateFeedstockBatchMaterialSnapshotCommand(
          workspaceId,
          FeedstockBatchId.of(source.reference().id()),
          source.selectedVolume(),
          source.aggregateRevision(),
          new FeedstockBatchMaterialEvidenceExpectation(
              new FeedstockBatchMaterialEvidenceReference(source.evidenceReference().value()),
              source.materialStateId()
          )
      ));
      return Boolean.TRUE;
    });
  }
}
