package org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase;

import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Applies Feedstock-owned workspace and available-volume rules to a loaded material batch. */
final class FeedstockMaterialAvailabilityPolicy {

  private FeedstockMaterialAvailabilityPolicy() {
  }

  static void requireAvailable(
      @NonNull FeedstockBatch batch,
      @NonNull WorkspaceId workspaceId,
      @NonNull VolumeValue requestedVolume
  ) {
    requireOwnedBy(batch, workspaceId);
    requireAvailableVolume(batch, requestedVolume);
  }

  static void requireOwnedBy(
      @NonNull FeedstockBatch batch,
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(batch, "feedstock batch cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    if (!batch.workspaceId().equals(workspaceId)) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          workspaceId,
          batch.workspaceId()
      );
    }
  }

  static void requireAvailableVolume(
      @NonNull FeedstockBatch batch,
      @NonNull VolumeValue requestedVolume
  ) {
    requireNonNull(batch, "feedstock batch cannot be null");
    requireNonNull(requestedVolume, "requested feedstock volume cannot be null");
    if (requestedVolume.exceeds(batch.availableVolume())) {
      throw new FeedstockBatchInsufficientAvailableVolumeException(
          batch.batchId(),
          requestedVolume,
          batch.availableVolume()
      );
    }
  }
}
