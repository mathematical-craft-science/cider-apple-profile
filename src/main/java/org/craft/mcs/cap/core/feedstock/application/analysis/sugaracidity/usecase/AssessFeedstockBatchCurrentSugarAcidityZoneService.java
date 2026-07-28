package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for assessing the current profile of a feedstock batch on the sugar-acidity
 * graph backed by SG/TA evidence.
 *
 * <p>Current means the state-aware composite selected by
 * {@link FeedstockCurrentProfileQueryService}. The service returns an unplottable result
 * when sugar-acidity graph capability is missing so clients can show actionable warnings instead of
 * hiding incomplete batches.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentSugarAcidityZoneService
    implements AssessFeedstockBatchCurrentSugarAcidityZoneUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;

  @Override
  public @NonNull FeedstockBatchCurrentSugarAcidityZoneResult assess(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var currentMeasurement = currentProfileQueryService.requireCurrentProfile(batchId);
    final var evaluation = chemicalProfileAssessmentAdapter.project(currentMeasurement.chemicalProfile());
    final var latestMeasurement = currentMeasurement.latestMeasurement();

    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentMeasurement.batchId(),
        batch.name(),
        batch.workspaceId(),
        currentMeasurement.currentMaterialStateId(),
        latestMeasurement.measurementId(),
        latestMeasurement.measuredAt(),
        evaluation
    );
  }
}
