package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZonePointBatch;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.UnplottableFeedstockSampleSugarAcidityZonePoint;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UnplottableFeedstockSampleSugarAcidityZonePointResponse;
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
        assessment.sampleId().value(),
        assessment.ciderMakerId().value(),
        assessment.batchId().value(),
        assessment.sampleName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }

  public static @NonNull FeedstockSampleSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockSampleSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock sample sugar-acidity zone result cannot be null");

    return new FeedstockSampleSugarAcidityZoneResponse(
        result.sampleId().value(),
        result.ciderMakerId().value(),
        result.batchId().value(),
        result.sampleName().value(),
        result.plottable(),
        result.assessment() == null ? null : SugarAcidityZoneRestMapper.toResponse(result.assessment()), // NOSONAR
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull FeedstockSampleSugarAcidityZonePointBatchResponse toResponse(
      @NonNull FeedstockSampleSugarAcidityZonePointBatch batch
  ) {
    requireNonNull(batch, "feedstock sample sugar-acidity zone point batch cannot be null");

    return new FeedstockSampleSugarAcidityZonePointBatchResponse(
        batch.points().stream()
            .map(FeedstockSampleSugarAcidityZoneRestMapper::toResponse)
            .toList(),
        batch.unplottable().stream()
            .map(FeedstockSampleSugarAcidityZoneRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull UnplottableFeedstockSampleSugarAcidityZonePointResponse toResponse(
      @NonNull UnplottableFeedstockSampleSugarAcidityZonePoint unplottable
  ) {
    requireNonNull(unplottable, "unplottable feedstock sample sugar-acidity zone point cannot be null");

    return new UnplottableFeedstockSampleSugarAcidityZonePointResponse(
        unplottable.sampleId().value(),
        unplottable.ciderMakerId().value(),
        unplottable.batchId().value(),
        unplottable.sampleName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(unplottable.capabilityAssessment())
    );
  }
}
