package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.fermentation.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class PredictFeedstockBatchCurrentPotentialAbvService
    implements PredictFeedstockBatchCurrentPotentialAbvUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    final var currentMeasurement = feedstockMeasurementQueryRepository.findLatestByBatchId(batchId)
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batchId));
    final var axes = axisExtractor.extract(currentMeasurement.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var sugar = axes.valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));

    return new FeedstockBatchCurrentPotentialAbvPrediction(
        currentMeasurement.batchId(),
        batch.ciderMakerId(),
        currentMeasurement.measurementId(),
        currentMeasurement.name(),
        sugar.valueKind(),
        predictor.predict(SpecificGravityValue.of(sugar.value()), SpecificGravityValue.of("1.000"))
    );
  }
}
