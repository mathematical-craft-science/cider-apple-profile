package org.craft.mcs.cap.core.measurement.application.contract;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Application read model for one immutable measurement record. */
public record MeasurementRecordView(
    @NonNull MeasurementRecordId recordId,
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementRecordRole role,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance,
    @Nullable MeasurementDerivationMethod derivationMethod,
    @NonNull List<@NonNull MeasurementRecordId> derivationInputs
) {

  public MeasurementRecordView {
    requireNonNull(recordId, "measurement record view id cannot be null");
    requireNonNull(characteristic, "measurement record view characteristic cannot be null");
    requireNonNull(role, "measurement record view role cannot be null");
    requireNonNull(value, "measurement record view value cannot be null");
    requireNonNull(measuredAt, "measurement record view time cannot be null");
    requireNonNull(provenance, "measurement record view provenance cannot be null");
    derivationInputs = List.copyOf(requireNonNull(
        derivationInputs,
        "measurement record view derivation inputs cannot be null"
    ));
  }
}
