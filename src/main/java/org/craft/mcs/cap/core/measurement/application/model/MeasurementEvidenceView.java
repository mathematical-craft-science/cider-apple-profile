package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Per-characteristic evidence selected for a current material profile. */
public record MeasurementEvidenceView(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementRecordId recordId,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance,
    @NonNull MeasurementFreshnessStatus freshnessStatus
) {

  public MeasurementEvidenceView {
    requireNonNull(characteristic, "measurement evidence characteristic cannot be null");
    requireNonNull(measurementSetId, "measurement evidence set id cannot be null");
    requireNonNull(recordId, "measurement evidence record id cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(provenance, "measurement evidence provenance cannot be null");
    requireNonNull(freshnessStatus, "measurement evidence freshness cannot be null");
  }
}
