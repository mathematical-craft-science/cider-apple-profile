package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FeedstockSampleSugarAcidityZoneResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String batchId,
    @NonNull String sampleName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {

  public FeedstockSampleSugarAcidityZoneResponse(
      @NonNull String sampleId,
      @NonNull String ciderMakerId,
      @NonNull String batchId,
      @NonNull String sampleName,
      @NonNull SugarAcidityZoneAssessmentResponse assessment
  ) {
    this(sampleId, ciderMakerId, batchId, sampleName, true, assessment, null);
  }
}
