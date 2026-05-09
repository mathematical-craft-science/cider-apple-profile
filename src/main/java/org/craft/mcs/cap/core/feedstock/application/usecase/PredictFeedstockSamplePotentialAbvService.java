package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockSamplePotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class PredictFeedstockSamplePotentialAbvService
    implements PredictFeedstockSamplePotentialAbvUseCase {

  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull FeedstockSamplePotentialAbvPrediction predict(
      @NonNull FeedstockSampleId sampleId
  ) {
    requireNonNull(sampleId, "feedstock sample id cannot be null");

    final var sample = feedstockSampleRepository.findById(sampleId)
        .orElseThrow(() -> new IllegalArgumentException("feedstock sample not found: " + sampleId.value()));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new IllegalStateException("feedstock sample batch not found: " + sample.batchId().value()));
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var sugar = axes.valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));

    return new FeedstockSamplePotentialAbvPrediction(
        sample.sampleId(),
        batch.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        sugar.valueKind(),
        predictor.predict(SpecificGravityValue.of(sugar.value()), SpecificGravityValue.of("1.000"))
    );
  }
}
