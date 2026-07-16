package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.AssessFeedstockMeasurementSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for assessing one historical feedstock measurement on the sugar-acidity
 * graph.
 *
 * <p>This use case exists beside the batch-current assessment because users may need to inspect or
 * correct past measurements. It does not apply the latest-measurement rule; it evaluates the exact
 * measurement requested by id.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockMeasurementSugarAcidityZoneService
    implements AssessFeedstockMeasurementSugarAcidityZoneUseCase {

  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull FeedstockMeasurementSugarAcidityZoneResult assess(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    final var sample = feedstockMeasurementRepository.findById(measurementId)
        .orElseThrow(() -> new FeedstockMeasurementNotFoundException(measurementId));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(sample.batchId()));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var evaluation = sugarAcidityProfileProjector.project(sample.chemicalProfile());

    return new FeedstockMeasurementSugarAcidityZoneResult(
        sample.measurementId(),
        batch.workspaceId(),
        sample.batchId(),
        sample.name(),
        evaluation
    );
  }
}
