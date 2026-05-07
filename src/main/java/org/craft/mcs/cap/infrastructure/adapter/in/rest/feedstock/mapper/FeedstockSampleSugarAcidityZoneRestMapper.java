package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockSampleSugarAcidityZoneRestMapper {

  private FeedstockSampleSugarAcidityZoneRestMapper() {
  }

  public static @NonNull FeedstockSampleSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockSampleSugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "feedstock sample sugar-acidity zone assessment cannot be null");

    return new FeedstockSampleSugarAcidityZoneResponse(
        assessment.sampleId().value().toString(),
        assessment.ciderMakerId().value().toString(),
        assessment.sampleName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }
}
