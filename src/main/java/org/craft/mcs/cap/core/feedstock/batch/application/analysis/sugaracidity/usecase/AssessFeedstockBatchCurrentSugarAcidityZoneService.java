package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.usecase;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TITRATABLE_ACIDITY;


/**
 * Application service for assessing the current profile of a feedstock batch on the sugar-acidity
 * graph backed by SG/TA evidence.
 *
 * <p>Current means the state-aware composite selected by
 * {@link FeedstockCurrentProfileQueryService}. The service returns an unplottable result
 * when SG or TA graph input is missing so clients can show actionable warnings instead of
 * hiding incomplete batches.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentSugarAcidityZoneService
    implements AssessFeedstockBatchCurrentSugarAcidityZoneUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;

  @Override
  public @NonNull FeedstockBatchCurrentSugarAcidityZoneResult assess(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var currentMeasurement = currentProfileQueryService.requireCurrentProfile(batchId);
    final var graphEvaluation = graphEvaluator.evaluate(
        currentMeasurement.specificGravity(),
        currentMeasurement.titratableAcidity()
    );
    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentMeasurement.batchId(),
        batch.name(),
        batch.workspaceId(),
        currentMeasurement.currentMaterialStateId(),
        currentMeasurement.latestMeasurementSet(),
        currentMeasurement.evidenceFor(Set.of(SPECIFIC_GRAVITY, TITRATABLE_ACIDITY)),
        currentMeasurement.specificGravity(),
        currentMeasurement.titratableAcidity(),
        graphEvaluation,
        graphEvaluation.plottable() && currentMeasurement.tannin() != null
            ? northAmericanProfileCalculator.assessTannin(currentMeasurement.tannin()) // NOSONAR
            : null
    );
  }
}
