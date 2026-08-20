package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Partial correction command that copies unchanged evidence into a new immutable revision. */
public record PatchMeasurementSetCommand(
    @NonNull MeasurementSetId measurementSetId,
    long expectedVersion,
    @Nullable MeasurementSetName name,
    boolean noteSpecified,
    @Nullable String note,
    @NonNull MeasurementCorrectionReason correctionReason,
    @NonNull List<@NonNull MeasurementCharacteristicInput> replacements,
    @NonNull Set<@NonNull MeasurementCharacteristic> removals
) {

  public PatchMeasurementSetCommand {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(correctionReason, "measurement correction reason cannot be null");
    replacements = List.copyOf(requireNonNull(replacements, "measurement replacements cannot be null"));
    removals = Set.copyOf(requireNonNull(removals, "measurement removals cannot be null"));
    if (expectedVersion < 0) {
      throw new ApplicationValidationException("expected measurement-set version cannot be negative");
    }
    if (name == null && !noteSpecified && replacements.isEmpty() && removals.isEmpty()) {
      throw new ApplicationValidationException("measurement patch requires at least one change");
    }
    if (replacements.stream()
        .map(MeasurementCharacteristicInput::characteristic)
        .anyMatch(removals::contains)) {
      throw new ApplicationValidationException(
          "measurement characteristic cannot be replaced and removed in the same correction"
      );
    }
  }
}
