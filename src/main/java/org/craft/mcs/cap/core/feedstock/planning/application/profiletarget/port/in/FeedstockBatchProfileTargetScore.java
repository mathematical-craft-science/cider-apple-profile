package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchProfileTargetScore(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull MeasurementEvidenceView> profileEvidence,
    @NonNull FeedstockTargetCharacteristics characteristics,
    @NonNull FeedstockTargetDistanceView distance,
    @NonNull FeedstockContributionView contribution
) {

  public FeedstockBatchProfileTargetScore {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    profileEvidence = List.copyOf(
        requireNonNull(profileEvidence, "feedstock target evidence cannot be null")
    );
    requireNonNull(characteristics, "feedstock target-scoring characteristics cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contribution, "profile target contribution cannot be null");
  }
}
