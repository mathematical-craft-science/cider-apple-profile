package org.craft.mcs.cap.core.feedstock.application.analysis.readiness.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.application.compatibility.ChemicalProfileFermentationStartAdapter;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.AssessFeedstockBatchCurrentFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.FeedstockBatchCurrentFermentationStartReadinessResult;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for assessing whether the current feedstock material state is ready to start
 * fermentation.
 *
 * <p>Historical measurements remain visible in batch history. The assessment uses only evidence
 * applicable to the current material-state lineage, selected independently per characteristic.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentFermentationStartReadinessService
    implements AssessFeedstockBatchCurrentFermentationStartReadinessUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull FermentationStartReadinessAssessor readinessAssessor;

  @Override
  public @NonNull FeedstockBatchCurrentFermentationStartReadinessResult assess(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var measurement = currentProfileQueryService.requireCurrentProfile(batchId);
    final var latestMeasurement = measurement.latestMeasurement();

    return new FeedstockBatchCurrentFermentationStartReadinessResult(
        batch.batchId(),
        batch.workspaceId(),
        measurement.currentMaterialStateId(),
        latestMeasurement.measurementId(),
        latestMeasurement.name(),
        readinessAssessor.assess(
            ChemicalProfileFermentationStartAdapter.adapt(measurement.chemicalProfile())
        )
    );
  }
}
