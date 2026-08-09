package org.craft.mcs.cap.core.feedstock.application.analysis.abv.usecase;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.FeedstockPotentialAbvUnavailableException;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Application service for potential ABV prediction from the current feedstock batch profile.
 *
 * <p>The service uses Specific Gravity evidence applicable to the current material state and
 * delegates ABV analysis math to the configured {@link PotentialAbvPredictor}. Final-gravity
 * assumptions belong to the configured prediction strategy, not to this use case.
 */
@Component
@RequiredArgsConstructor
public final class PredictFeedstockBatchCurrentPotentialAbvService
    implements PredictFeedstockBatchCurrentPotentialAbvUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var currentMeasurement = currentProfileQueryService.requireCurrentProfile(batchId);
    final var specificGravity = currentMeasurement.specificGravity();
    if (specificGravity == null) {
      throw new FeedstockPotentialAbvUnavailableException(
          batchId
      );
    }

    final var specificGravityEvidence = currentMeasurement.evidenceFor(Set.of(SPECIFIC_GRAVITY))
        .getFirst();

    return new FeedstockBatchCurrentPotentialAbvPrediction(
        currentMeasurement.batchId(),
        batch.workspaceId(),
        currentMeasurement.currentMaterialStateId(),
        specificGravityEvidence,
        specificGravity,
        predictor.predict(SpecificGravityValue.of(midpoint(specificGravity)))
    );
  }
}
