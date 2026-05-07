package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record UnplottableFeedstockSampleSugarAcidityZonePoint(
    @NonNull FeedstockSampleId sampleId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockSampleName sampleName,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public UnplottableFeedstockSampleSugarAcidityZonePoint {
    requireNonNull(sampleId, "feedstock sample id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(sampleName, "feedstock sample name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");
  }
}
