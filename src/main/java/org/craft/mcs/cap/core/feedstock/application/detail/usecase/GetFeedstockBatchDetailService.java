package org.craft.mcs.cap.core.feedstock.application.detail.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchDetailView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Builds feedstock batch details from existing batch, measurement and analysis read-side services.
 *
 * <p>The use case keeps detail assembly out of the aggregate model: batch and measurement slices
 * remain responsible for their own facts, while this service produces one coherent current-profile
 * snapshot for API clients.
 */
@Component
@RequiredArgsConstructor
public final class GetFeedstockBatchDetailService implements GetFeedstockBatchDetailUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull ListFeedstockMeasurementsUseCase listFeedstockMeasurementsUseCase;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull FeedstockCurrentProfileAssessmentService currentProfileAssessmentService;

  @Override
  public @NonNull FeedstockBatchDetailView get(@NonNull GetFeedstockBatchDetailQuery query) {
    requireNonNull(query, "feedstock batch detail query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());

    final var batch = feedstockBatchRepository.findById(query.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(query.batchId()));
    if (!batch.workspaceId().equals(query.workspaceId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          query.workspaceId(),
          batch.workspaceId()
      );
    }

    final var batchSummary = FeedstockBatchSummaryMapper.toSummary(batch);
    final var measurements = listFeedstockMeasurementsUseCase.listByBatchId(batch.batchId());
    final var currentMeasurement = currentProfileQueryService.findCurrentProfile(batchSummary)
        .orElse(null); // NOSONAR
    final var latestMeasurement = latestContributingMeasurement(currentMeasurement, measurements);

    return new FeedstockBatchDetailView(
        batchSummary,
        toCurrentProfile(
            batchSummary,
            currentMeasurement,
            latestMeasurement
        ),
        measurements
    );
  }

  private @NonNull FeedstockBatchCurrentProfileView toCurrentProfile(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile,
      @Nullable FeedstockMeasurementSummary latestMeasurement
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    if (currentProfile == null) {
      return new FeedstockBatchCurrentProfileView(
          latestMeasurement,
          null,
          currentProfileAssessmentService.assess(batch, null)
      );
    }

    return new FeedstockBatchCurrentProfileView(
        latestMeasurement,
        currentProfile.summary(),
        currentProfileAssessmentService.assess(batch, currentProfile)
    );
  }

  private static @Nullable FeedstockMeasurementSummary latestContributingMeasurement(
      @Nullable FeedstockCurrentProfileView currentProfile,
      @NonNull List<@NonNull FeedstockMeasurementSummary> measurements
  ) {
    requireNonNull(measurements, "feedstock measurement summaries cannot be null");
    if (currentProfile == null) {
      return null;
    }
    final var measurementId = currentProfile.latestMeasurement().measurementId();
    final var version = currentProfile.latestMeasurement().version();
    return measurements.stream()
        .filter(measurement -> measurement.measurementId().equals(measurementId)
            && measurement.version() == version)
        .findFirst()
        .orElse(null); // NOSONAR
  }

}
