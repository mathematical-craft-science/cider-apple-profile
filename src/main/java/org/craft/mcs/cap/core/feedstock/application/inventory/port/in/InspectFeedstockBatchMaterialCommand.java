package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command to inspect feedstock evidence for a proposed material volume without consuming it. */
public record InspectFeedstockBatchMaterialCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockBatchId batchId,
    @NonNull VolumeValue requestedVolume
) {

  public InspectFeedstockBatchMaterialCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(requestedVolume, "requested feedstock volume cannot be null");
  }
}
