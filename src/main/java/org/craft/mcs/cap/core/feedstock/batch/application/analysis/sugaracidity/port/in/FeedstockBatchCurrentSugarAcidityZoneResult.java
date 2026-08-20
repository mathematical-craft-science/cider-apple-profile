package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Current feedstock-batch identity bound to its typed SG/TA graph evaluation.
 */
public record FeedstockBatchCurrentSugarAcidityZoneResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull BatchName batchName,
    @NonNull WorkspaceId workspaceId,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull FeedstockLatestMeasurementSetView latestMeasurementSet,
    @NonNull List<@NonNull MeasurementEvidenceView> profileEvidence,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @NonNull FeedstockSugarAcidityGraphView graphEvaluation,
    @Nullable FeedstockNorthAmericanTanninLevel northAmericanTanninLevel
) {

  public FeedstockBatchCurrentSugarAcidityZoneResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(latestMeasurementSet, "latest feedstock measurement set cannot be null");
    profileEvidence = List.copyOf(requireNonNull(profileEvidence, "feedstock graph evidence cannot be null"));
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
  }
}
