package org.craft.mcs.cap.core.feedstock.application.analysis.readiness.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.AssessFeedstockBatchCurrentFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.FeedstockBatchCurrentFermentationStartReadinessResult;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
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
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull FermentationStartReadinessAssessor readinessAssessor;

  @Override
  public @NonNull FeedstockBatchCurrentFermentationStartReadinessResult assess(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var measurement = currentMeasurementQueryService.requireCurrentMeasurement(batchId);

    return new FeedstockBatchCurrentFermentationStartReadinessResult(
        batch.batchId(),
        batch.workspaceId(),
        measurement.measurementId(),
        measurement.name(),
        readinessAssessor.assess(measurement.chemicalProfile())
    );
  }
}
