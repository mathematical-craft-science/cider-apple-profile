package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentFermentationStartReadinessResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull MeasurementEvidenceView> profileEvidence,
    @NonNull FermentationStartReadinessAssessment assessment
) {

  public FeedstockBatchCurrentFermentationStartReadinessResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    profileEvidence = List.copyOf(
        requireNonNull(profileEvidence, "fermentation readiness evidence cannot be null")
    );
    requireNonNull(assessment, "fermentation start readiness assessment cannot be null");
  }
}
