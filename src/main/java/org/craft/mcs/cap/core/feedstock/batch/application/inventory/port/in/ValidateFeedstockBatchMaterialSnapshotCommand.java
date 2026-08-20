package org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Previously inspected Feedstock tokens that must still be current before candidate persistence. */
public record ValidateFeedstockBatchMaterialSnapshotCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockBatchId batchId,
    @NonNull VolumeValue requestedVolume,
    @NonNull AggregateRevision aggregateRevision,
    @NonNull FeedstockBatchMaterialEvidenceExpectation expectation
) {

  public ValidateFeedstockBatchMaterialSnapshotCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(requestedVolume, "requested feedstock volume cannot be null");
    requireNonNull(aggregateRevision, "feedstock batch aggregate revision cannot be null");
    requireNonNull(expectation, "feedstock material evidence expectation cannot be null");
  }
}
