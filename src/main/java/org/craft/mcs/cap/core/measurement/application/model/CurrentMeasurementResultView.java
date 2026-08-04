package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Current active RESULT record returned by the measurement read side. */
public record CurrentMeasurementResultView(
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull MaterialStateId evidenceMaterialStateId,
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementRecordId recordId,
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance
) {

  public CurrentMeasurementResultView {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(evidenceMaterialStateId, "evidence material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "measurement value cannot be null");
    requireNonNull(measuredAt, "measurement time cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
  }
}
