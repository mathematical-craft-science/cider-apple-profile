package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.usecase;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockFermentationStartReadinessAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.in.AssessFeedstockBatchCurrentFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.in.FeedstockBatchCurrentFermentationStartReadinessResult;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.PH;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.YAN;


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
  private final @NonNull FeedstockFermentationStartReadinessAnalyzer readinessAnalyzer;

  @Override
  public @NonNull FeedstockBatchCurrentFermentationStartReadinessResult assess(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var measurement = currentProfileQueryService.requireCurrentProfile(batchId);
    return new FeedstockBatchCurrentFermentationStartReadinessResult(
        batch.batchId(),
        batch.workspaceId(),
        measurement.currentMaterialStateId(),
        measurement.evidenceFor(Set.of(SPECIFIC_GRAVITY, PH, YAN)),
        readinessAnalyzer.assess(
            measurement.specificGravity(),
            measurement.ph(),
            measurement.yeastAssimilableNitrogen()
        )
    );
  }
}
