package org.craft.mcs.cap.core.feedstock.application.analysis.abv.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockMeasurementPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.PredictFeedstockMeasurementPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for potential ABV prediction from one historical feedstock measurement.
 *
 * <p>This use case evaluates the exact measurement requested by id, which is useful when comparing
 * how projected alcohol changed across the measurement timeline. Batch-current ABV prediction uses
 * the latest-measurement rule and is exposed as a separate use case.
 */
@Component
@RequiredArgsConstructor
public final class PredictFeedstockMeasurementPotentialAbvService
    implements PredictFeedstockMeasurementPotentialAbvUseCase {

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockMeasurementPotentialAbvPrediction predict(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    final var sample = feedstockMeasurementQueryRepository.findById(measurementId)
        .orElseThrow(() -> new FeedstockMeasurementNotFoundException(measurementId));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(sample.batchId()));
    feedstockAccessControl.requireReadAccess(batch.workspaceId());
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var specificGravity = axes.requireValueOf(ChemicalAxis.SPECIFIC_GRAVITY);

    return new FeedstockMeasurementPotentialAbvPrediction(
        sample.measurementId(),
        batch.workspaceId(),
        sample.batchId(),
        sample.name(),
        specificGravity.valueKind(),
        predictor.predict(SpecificGravityValue.of(specificGravity.value()))
    );
  }
}
