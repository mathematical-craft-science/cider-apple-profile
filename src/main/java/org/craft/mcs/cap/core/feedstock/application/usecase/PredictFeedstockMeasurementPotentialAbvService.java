package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockMeasurementPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.core.fermentation.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class PredictFeedstockMeasurementPotentialAbvService
    implements PredictFeedstockMeasurementPotentialAbvUseCase {

  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockMeasurementPotentialAbvPrediction predict(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    final var sample = feedstockMeasurementRepository.findById(measurementId)
        .orElseThrow(() -> new IllegalArgumentException("feedstock measurement not found: " + measurementId.value()));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new IllegalStateException("feedstock measurement batch not found: " + sample.batchId().value()));
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var sugar = axes.valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));

    return new FeedstockMeasurementPotentialAbvPrediction(
        sample.measurementId(),
        batch.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        sugar.valueKind(),
        predictor.predict(SpecificGravityValue.of(sugar.value()), SpecificGravityValue.of("1.000"))
    );
  }
}
