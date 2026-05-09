package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZonePointBatch;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSampleSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.UnplottableFeedstockSampleSugarAcidityZonePoint;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public final class ListFeedstockSampleSugarAcidityZonePointsService
    implements ListFeedstockSampleSugarAcidityZonePointsUseCase {

  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull FeedstockSampleSugarAcidityZonePointBatch listByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  ) {
    final List<@NonNull FeedstockSampleSugarAcidityZoneAssessment> points = new ArrayList<>();
    final List<@NonNull UnplottableFeedstockSampleSugarAcidityZonePoint> unplottable = new ArrayList<>();

    for (final var sample : feedstockSampleRepository.findAllByCiderMakerId(ciderMakerId)) {
      final var axes = axisExtractor.extract(sample.chemicalProfile());
      final var capability = capabilityAssessor.assess(
          axes,
          ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
      );

      if (capability.capable()) {
        assessor.assess(axes)
            .map(assessment -> toAssessment(sample, assessment))
            .ifPresent(points::add);
      } else {
        unplottable.add(toUnplottable(sample, capability));
      }
    }

    return new FeedstockSampleSugarAcidityZonePointBatch(points, unplottable);
  }

  private static @NonNull FeedstockSampleSugarAcidityZoneAssessment toAssessment(
      @NonNull FeedstockSample sample,
      @NonNull SugarAcidityZoneAssessment assessment
  ) {
    return new FeedstockSampleSugarAcidityZoneAssessment(
        sample.sampleId(),
        sample.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        assessment
    );
  }

  private static @NonNull UnplottableFeedstockSampleSugarAcidityZonePoint toUnplottable(
      @NonNull FeedstockSample sample,
      @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
  ) {
    return new UnplottableFeedstockSampleSugarAcidityZonePoint(
        sample.sampleId(),
        sample.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        capabilityAssessment
    );
  }
}
