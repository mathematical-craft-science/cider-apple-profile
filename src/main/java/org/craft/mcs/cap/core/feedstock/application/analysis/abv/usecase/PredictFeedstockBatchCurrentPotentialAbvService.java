package org.craft.mcs.cap.core.feedstock.application.analysis.abv.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for potential ABV prediction from the current feedstock batch profile.
 *
 * <p>The service uses the latest measurement of the batch, verifies that sugar is present through
 * {@link ChemicalProfileCapability#ABV_PREDICTION}, then delegates ABV analysis math to the
 * configured {@link PotentialAbvPredictor}. Final gravity assumptions belong to the configured
 * prediction strategy, not to this use case.
 */
@Component
@RequiredArgsConstructor
public final class PredictFeedstockBatchCurrentPotentialAbvService
    implements PredictFeedstockBatchCurrentPotentialAbvUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var currentMeasurement = currentMeasurementQueryService.requireCurrentMeasurement(batchId);
    final var axes = axisExtractor.extract(currentMeasurement.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var sugar = axes.requireValueOf(ChemicalAxis.SUGAR);

    return new FeedstockBatchCurrentPotentialAbvPrediction(
        currentMeasurement.batchId(),
        batch.workspaceId(),
        currentMeasurement.measurementId(),
        currentMeasurement.name(),
        sugar.valueKind(),
        predictor.predict(SpecificGravityValue.of(sugar.value()))
    );
  }
}
