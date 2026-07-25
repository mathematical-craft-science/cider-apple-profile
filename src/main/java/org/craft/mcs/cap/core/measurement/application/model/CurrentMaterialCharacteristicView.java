package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** One state-aware characteristic selected from immutable measurement evidence. */
public record CurrentMaterialCharacteristicView(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId evidenceMaterialStateId,
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementRecordId recordId,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance,
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @NonNull MeasurementEvidenceValidity validity
) {

  public CurrentMaterialCharacteristicView {
    requireNonNull(characteristic, "current material characteristic cannot be null");
    requireNonNull(value, "current material value cannot be null");
    requireNonNull(origin, "current material value origin cannot be null");
    requireNonNull(evidenceMaterialStateId, "evidence material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement time cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
    requireNonNull(freshnessStatus, "measurement freshness status cannot be null");
    requireNonNull(validity, "current material value validity cannot be null");
  }
}
