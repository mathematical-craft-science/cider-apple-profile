package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockMeasurementSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for assessing one historical feedstock measurement on the sugar-acidity
 * graph.
 *
 * <p>This use case exists beside the batch-current assessment because users may need to inspect or
 * correct past measurements. It does not apply the latest-measurement rule; it evaluates the exact
 * measurement requested by id.
 */
@Component
@RequiredArgsConstructor
public final class AssessFeedstockMeasurementSugarAcidityZoneService
    implements AssessFeedstockMeasurementSugarAcidityZoneUseCase {

  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull FeedstockMeasurementSugarAcidityZoneResult assess(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    final var sample = feedstockMeasurementRepository.findById(measurementId)
        .orElseThrow(() -> new IllegalArgumentException("feedstock measurement not found: " + measurementId.value()));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new IllegalStateException("feedstock measurement batch not found: " + sample.batchId().value()));
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var capability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    if (!capability.capable()) {
      return new FeedstockMeasurementSugarAcidityZoneResult(
          sample.measurementId(),
          batch.ciderMakerId(),
          sample.batchId(),
          sample.name(),
          false,
          null,
          capability
      );
    }

    final var assessment = assessor.assess(axes)
        .orElseThrow(() -> new IllegalStateException(
            "sugar-acidity assessor did not assess capable feedstock measurement profile: " + measurementId.value()
        ));

    return new FeedstockMeasurementSugarAcidityZoneResult(
        sample.measurementId(),
        batch.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        true,
        assessment,
        capability
    );
  }
}
