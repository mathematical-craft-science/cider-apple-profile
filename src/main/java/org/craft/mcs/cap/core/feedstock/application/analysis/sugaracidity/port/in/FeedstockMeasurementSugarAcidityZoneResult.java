package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityZoneResult(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName measurementName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessment assessment,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public FeedstockMeasurementSugarAcidityZoneResult {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementName, "feedstock measurement name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");

    if (plottable && assessment == null) {
      throw new ApplicationValidationException("plottable feedstock measurement result must contain assessment");
    }
  }
}
