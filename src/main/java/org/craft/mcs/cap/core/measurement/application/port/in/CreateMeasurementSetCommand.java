package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Command for recording the initial immutable revision of a logical measurement set. */
public record CreateMeasurementSetCommand(
    @Nullable MeasurementSetId requestedId,
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId expectedCurrentStateId,
    @NonNull MeasurementSetName name,
    @Nullable String note,
    @NonNull List<@NonNull MeasurementCharacteristicInput> measurements
) {

  public CreateMeasurementSetCommand {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(expectedCurrentStateId, "expected current material state id cannot be null");
    requireNonNull(name, "measurement set name cannot be null");
    measurements = List.copyOf(requireNonNull(measurements, "measurement inputs cannot be null"));
  }
}
