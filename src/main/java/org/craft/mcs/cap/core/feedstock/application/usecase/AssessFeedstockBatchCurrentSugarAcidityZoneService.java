package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class AssessFeedstockBatchCurrentSugarAcidityZoneService
    implements AssessFeedstockBatchCurrentSugarAcidityZoneUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull FeedstockBatchCurrentSugarAcidityZoneResult assess(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
    final var currentMeasurement = feedstockMeasurementQueryRepository.findLatestByBatchId(batchId)
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batchId));
    final var axes = axisExtractor.extract(currentMeasurement.chemicalProfile());
    final var capability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    if (!capability.capable()) {
      return new FeedstockBatchCurrentSugarAcidityZoneResult(
          currentMeasurement.batchId(),
          batch.ciderMakerId(),
          currentMeasurement.measurementId(),
          currentMeasurement.name(),
          false,
          null,
          capability
      );
    }

    final var assessment = assessor.assess(axes)
        .orElseThrow(() -> new IllegalStateException(
            "sugar-acidity assessor did not assess capable feedstock batch current profile: " + batchId.value()
        ));

    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentMeasurement.batchId(),
        batch.ciderMakerId(),
        currentMeasurement.measurementId(),
        currentMeasurement.name(),
        true,
        assessment,
        capability
    );
  }
}
