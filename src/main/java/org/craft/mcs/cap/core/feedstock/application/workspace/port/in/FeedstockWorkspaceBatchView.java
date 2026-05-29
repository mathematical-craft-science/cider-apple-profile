package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockWorkspaceBatchView(
    @NonNull FeedstockBatchSummary batch,
    @Nullable FeedstockWorkspaceCurrentMeasurementView currentMeasurement,
    @Nullable SugarAcidityProfileEvaluation sugarAcidity,
    @Nullable FeedstockWorkspacePotentialAbvView potentialAbv,
    @Nullable AxisValue yeastAssimilableNitrogen,
    @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
    @Nullable FeedstockTargetCandidateView targetCandidate
) {

  public FeedstockWorkspaceBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
  }
}
