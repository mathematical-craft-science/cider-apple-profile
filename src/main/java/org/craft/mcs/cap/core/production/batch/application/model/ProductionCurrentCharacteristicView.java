package org.craft.mcs.cap.core.production.batch.application.model;

import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * One effective Production characteristic with an explicit source.
 *
 * <p>The view keeps a measured value bound to its own evidence while an inherited value points
 * back to the batch's initial calculated snapshot.</p>
 */
public record ProductionCurrentCharacteristicView<T extends MeasuredValue>(
    @NonNull CharacteristicValue<T> value,
    @NonNull ProductionCharacteristicOrigin origin,
    @Nullable MeasurementEvidenceView evidence
) {

  public ProductionCurrentCharacteristicView {
    requireNonNull(value, "production characteristic value cannot be null");
    requireNonNull(origin, "production characteristic origin cannot be null");
    if (origin == ProductionCharacteristicOrigin.MEASUREMENT && evidence == null) {
      throw new ApplicationValidationException("measured production characteristic requires evidence");
    }
    if (origin == ProductionCharacteristicOrigin.INITIAL_SNAPSHOT && evidence != null) {
      throw new ApplicationValidationException(
          "initial production characteristic cannot have measurement evidence"
      );
    }
  }

  public static <T extends MeasuredValue> @NonNull ProductionCurrentCharacteristicView<T>
  fromInitialSnapshot(
      @NonNull CharacteristicValue<T> value
  ) {
    return new ProductionCurrentCharacteristicView<>(
        value,
        ProductionCharacteristicOrigin.INITIAL_SNAPSHOT,
        null
    );
  }

  public static <T extends MeasuredValue> @NonNull ProductionCurrentCharacteristicView<T>
  fromMeasurement(
      @NonNull CharacteristicValue<T> value,
      @NonNull MeasurementEvidenceView evidence
  ) {
    return new ProductionCurrentCharacteristicView<>(
        value,
        ProductionCharacteristicOrigin.MEASUREMENT,
        evidence
    );
  }
}
