package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockSampleSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class AssessFeedstockSampleSugarAcidityZoneService
    implements AssessFeedstockSampleSugarAcidityZoneUseCase {

  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull FeedstockSampleSugarAcidityZoneResult assess(
      @NonNull FeedstockSampleId sampleId
  ) {
    requireNonNull(sampleId, "feedstock sample id cannot be null");

    final var sample = feedstockSampleRepository.findById(sampleId)
        .orElseThrow(() -> new IllegalArgumentException("feedstock sample not found: " + sampleId.value()));
    final var batch = feedstockBatchRepository.findById(sample.batchId())
        .orElseThrow(() -> new IllegalStateException("feedstock sample batch not found: " + sample.batchId().value()));
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var capability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    if (!capability.capable()) {
      return new FeedstockSampleSugarAcidityZoneResult(
          sample.sampleId(),
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
            "sugar-acidity assessor did not assess capable feedstock sample profile: " + sampleId.value()
        ));

    return new FeedstockSampleSugarAcidityZoneResult(
        sample.sampleId(),
        batch.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        true,
        assessment,
        capability
    );
  }
}
