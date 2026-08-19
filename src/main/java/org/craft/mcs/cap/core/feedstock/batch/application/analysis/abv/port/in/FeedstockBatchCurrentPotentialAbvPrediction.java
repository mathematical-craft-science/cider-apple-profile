package org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.in;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentPotentialAbvPrediction(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull MeasurementEvidenceView specificGravityEvidence,
    @NonNull CharacteristicValue<SpecificGravityValue> originalGravity,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockBatchCurrentPotentialAbvPrediction {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(specificGravityEvidence, "specific gravity evidence cannot be null");
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
