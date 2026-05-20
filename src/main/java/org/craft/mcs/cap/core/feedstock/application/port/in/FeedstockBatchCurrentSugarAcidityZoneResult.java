package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentSugarAcidityZoneResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementId currentMeasurementId,
    @NonNull FeedstockMeasurementName currentMeasurementName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessment assessment,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public FeedstockBatchCurrentSugarAcidityZoneResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(currentMeasurementId, "current feedstock measurement id cannot be null");
    requireNonNull(currentMeasurementName, "current feedstock measurement name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");

    if (plottable && assessment == null) {
      throw new ApplicationValidationException("plottable feedstock batch current result must contain assessment");
    }
  }
}
