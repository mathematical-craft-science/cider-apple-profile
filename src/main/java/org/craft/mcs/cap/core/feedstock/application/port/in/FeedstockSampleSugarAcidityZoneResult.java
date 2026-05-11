package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityZoneResult(
    @NonNull FeedstockSampleId sampleId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockSampleName sampleName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessment assessment,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public FeedstockSampleSugarAcidityZoneResult {
    requireNonNull(sampleId, "feedstock sample id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(sampleName, "feedstock sample name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");

    if (plottable && assessment == null) {
      throw new IllegalArgumentException("plottable feedstock sample result must contain assessment");
    }
  }
}
