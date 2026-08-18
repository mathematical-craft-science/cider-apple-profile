package org.craft.mcs.cap.core.measurement.application.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.application.measurement.MeasurementCharacteristicValues.requireType;


/** State-aware projection of currently applicable measured characteristics for one material subject. */
public record CurrentMeasurementEvidenceView(
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull CurrentMeasurementCharacteristicView> characteristics
) {

  public CurrentMeasurementEvidenceView {
    requireNonNull(subject, "current measurement subject cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    characteristics = List.copyOf(
        requireNonNull(characteristics, "current measurement characteristics cannot be null")
    );
    final Set<MeasurementCharacteristic> uniqueCharacteristics = characteristics.stream()
        .map(CurrentMeasurementCharacteristicView::characteristic)
        .collect(Collectors.toUnmodifiableSet());
    if (uniqueCharacteristics.size() != characteristics.size()) {
      throw new ApplicationValidationException(
          "current measurement evidence must contain at most one value per characteristic"
      );
    }
  }

  /** Returns the currently applicable value and evidence for the requested characteristic. */
  public @NonNull Optional<@NonNull CurrentMeasurementCharacteristicView> characteristic(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    return characteristics.stream()
        .filter(value -> value.characteristic() == characteristic)
        .findFirst();
  }

  /** Returns one selected characteristic with its concrete canonical value type preserved. */
  public <T extends MeasuredValue> @NonNull Optional<@NonNull CharacteristicValue<T>> value(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull Class<T> valueType
  ) {
    requireNonNull(valueType, "measurement value type cannot be null");
    return characteristic(characteristic)
        .map(CurrentMeasurementCharacteristicView::value)
        .map(value -> requireType(characteristic, value, valueType));
  }
}
