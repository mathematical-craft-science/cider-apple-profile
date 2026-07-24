package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Identifies a material or process characteristic supported by generic measurement evidence.
 *
 * <p>The enum defines only the measurement taxonomy. Canonical units and range invariants belong to the
 * characteristic-specific value objects.</p>
 */
public enum MeasurementCharacteristic {

  /** Relative density used as the sugar and gravity characteristic. */
  SPECIFIC_GRAVITY,

  /** Acidity activity measured on the pH scale. */
  PH,

  /** Titratable acidity of the material. */
  TITRATABLE_ACIDITY,

  /** Tannin concentration of the material. */
  TANNIN,

  /** Yeast-assimilable nitrogen available in the material. */
  YAN,

  /** Temperature of the material or process environment. */
  TEMPERATURE,

  /** Turbidity of the material. */
  TURBIDITY;

  /** Verifies that a typed value belongs to this characteristic without redefining its unit or range. */
  public boolean supports(@NonNull MeasuredValue value) {
    requireNonNull(value, "measured value cannot be null");
    return switch (this) {
      case SPECIFIC_GRAVITY -> value instanceof SpecificGravityValue;
      case PH -> value instanceof PhValue;
      case TITRATABLE_ACIDITY -> value instanceof TitratableAcidityValue;
      case TANNIN -> value instanceof TanninValue;
      case YAN -> value instanceof YeastAssimilableNitrogenValue;
      case TEMPERATURE -> value instanceof TemperatureValue;
      case TURBIDITY -> value instanceof TurbidityValue;
    };
  }
}
