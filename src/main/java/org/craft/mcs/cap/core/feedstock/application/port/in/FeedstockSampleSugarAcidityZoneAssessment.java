package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityZoneAssessment(
    @NonNull FeedstockSampleId sampleId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockSampleName sampleName,
    @NonNull SugarAcidityZoneAssessment assessment
) {

  public FeedstockSampleSugarAcidityZoneAssessment {
    requireNonNull(sampleId, "feedstock sample id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(sampleName, "feedstock sample name cannot be null");
    requireNonNull(assessment, "sugar-acidity zone assessment cannot be null");
  }
}
