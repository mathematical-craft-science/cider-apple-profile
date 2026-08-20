package org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Expected Feedstock material state and evidence used to reject stale consumers. */
public record FeedstockBatchMaterialEvidenceExpectation(
    @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference,
    @NonNull MaterialStateId materialStateId
) {

  public FeedstockBatchMaterialEvidenceExpectation {
    requireNonNull(evidenceReference, "feedstock material evidence reference cannot be null");
    requireNonNull(materialStateId, "feedstock material state id cannot be null");
  }
}
