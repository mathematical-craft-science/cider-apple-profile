package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for assessing the current profile of a feedstock batch on the sugar-acidity
 * graph.
 *
 * <p>Current means the latest measurement selected by
 * {@link FeedstockBatchCurrentMeasurementQueryService}. The service returns an unplottable result
 * when sugar/acidity capability is missing so clients can show actionable warnings instead of
 * hiding incomplete batches.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentSugarAcidityZoneService
    implements AssessFeedstockBatchCurrentSugarAcidityZoneUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull FeedstockBatchCurrentSugarAcidityZoneResult assess(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    final var currentMeasurement = currentMeasurementQueryService.requireCurrentMeasurement(batchId);
    final var evaluation = sugarAcidityProfileProjector.project(currentMeasurement.chemicalProfile());

    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentMeasurement.batchId(),
        batch.name(),
        batch.ciderMakerId(),
        currentMeasurement.measurementId(),
        currentMeasurement.measuredAt(),
        evaluation
    );
  }
}
