package org.craft.mcs.cap.core.feedstock.application.analysis.readiness.usecase;

import lombok.RequiredArgsConstructor;
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
 * Application service for assessing whether the latest feedstock measurement is ready to start
 * fermentation.
 *
 * <p>The use case deliberately applies the batch-level "latest measurement only" rule: historical
 * measurements remain visible in batch history, but fermentation-start assessments are based on the
 * current measured profile of the feedstock batch.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentFermentationStartReadinessService
    implements AssessFeedstockBatchCurrentFermentationStartReadinessUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull FermentationStartReadinessAssessor readinessAssessor;

  @Override
  public @NonNull FeedstockBatchCurrentFermentationStartReadinessResult assess(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    final var measurement = currentMeasurementQueryService.requireCurrentMeasurement(batchId);

    return new FeedstockBatchCurrentFermentationStartReadinessResult(
        batch.batchId(),
        batch.ciderMakerId(),
        measurement.measurementId(),
        measurement.name(),
        readinessAssessor.assess(measurement.chemicalProfile())
    );
  }
}
