package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Coherent batch metadata and current-profile evidence selected from the same material state. */
public record FeedstockCurrentProfilePageItem(
    @NonNull FeedstockBatchSummary batch,
    @NonNull FeedstockCurrentProfileView currentProfile
) {

  public FeedstockCurrentProfilePageItem {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    if (!batch.batchId().equals(currentProfile.batchId())
        || !batch.currentMaterialStateId().equals(currentProfile.currentMaterialStateId())) {
      throw new ApplicationValidationException(
          "feedstock batch summary and current profile must describe the same material state"
      );
    }
  }
}
