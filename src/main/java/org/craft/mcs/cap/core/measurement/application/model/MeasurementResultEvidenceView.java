package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Active immutable RESULT evidence read from Measurement persistence before lineage selection. */
public record MeasurementResultEvidenceView(
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId materialStateId,
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementRecordId recordId,
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance
) {

  public MeasurementResultEvidenceView {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(materialStateId, "evidence material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "measurement value cannot be null");
    requireNonNull(measuredAt, "measurement time cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
    if (!characteristic.supports(value)) {
      throw new ApplicationValidationException(
          "measurement characteristic " + characteristic + " has an incompatible value"
      );
    }
  }
}
