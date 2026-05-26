package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.application.port.out.ProductionFeedstockTraceSnapshot;
import org.craft.mcs.cap.core.production.application.port.out.ProductionFeedstockTraceSnapshotProvider;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds production trace snapshots from the current feedstock measurement.
 *
 * <p>The adapter applies the feedstock context rule that a batch's actionable chemical profile is
 * its latest measurement. Production receives only a stable snapshot and does not need to know how
 * feedstock measurements are selected or normalized.
 */
@Component
@RequiredArgsConstructor
class FeedstockProductionTraceSnapshotProvider implements ProductionFeedstockTraceSnapshotProvider {

  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;

  @Override
  public @NonNull ProductionFeedstockTraceSnapshot currentSnapshotFor(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var measurement = currentMeasurementQueryService.requireCurrentMeasurement(batchId);
    final var profile = measurement.chemicalProfile();

    return new ProductionFeedstockTraceSnapshot(
        axisExtractor.extract(profile),
        profile.confidence()
    );
  }
}
