package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockSampleSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class AssessFeedstockSampleSugarAcidityZoneService
    implements AssessFeedstockSampleSugarAcidityZoneUseCase {

  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull FeedstockSampleSugarAcidityZoneAssessment assess(
      @NonNull FeedstockSampleId sampleId
  ) {
    requireNonNull(sampleId, "feedstock sample id cannot be null");

    final var sample = feedstockSampleRepository.findById(sampleId)
        .orElseThrow(() -> new IllegalArgumentException("feedstock sample not found: " + sampleId.value()));
    final var assessment = assessor.assess(axisExtractor.extract(sample.chemicalProfile()))
        .orElseThrow(() -> new IllegalArgumentException(
            "feedstock sample chemical profile must have sugar and acidity axes: " + sampleId.value()
        ));

    return new FeedstockSampleSugarAcidityZoneAssessment(
        sample.sampleId(),
        sample.ciderMakerId(),
        sample.name(),
        assessment
    );
  }
}
