package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSampleSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
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
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull List<@NonNull FeedstockSampleSugarAcidityZoneAssessment> listByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  ) {
    return feedstockSampleRepository.findAllByCiderMakerId(ciderMakerId)
        .stream()
        .flatMap(sample -> assessor.assess(axisExtractor.extract(sample.chemicalProfile()))
            .map(assessment -> toAssessment(sample, assessment))
            .stream())
        .toList();
  }

  private static @NonNull FeedstockSampleSugarAcidityZoneAssessment toAssessment(
      @NonNull FeedstockSample sample,
      @NonNull SugarAcidityZoneAssessment assessment
  ) {
    return new FeedstockSampleSugarAcidityZoneAssessment(
        sample.sampleId(),
        sample.ciderMakerId(),
        sample.name(),
        assessment
    );
  }
}
