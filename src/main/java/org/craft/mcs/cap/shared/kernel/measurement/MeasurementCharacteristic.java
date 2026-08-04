package org.craft.mcs.cap.shared.kernel.measurement;

import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Platform-wide taxonomy of measurable material and process characteristics.
 *
 * <p>The enum does not define units or ranges. Canonical units and value invariants belong to the
 * characteristic-specific {@link MeasuredValue} implementations.</p>
 */
public enum MeasurementCharacteristic {

  /** Relative density measurement; analysis may interpret it as sugar potential for unfermented material. */
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

  /** Verifies every endpoint of a point or interval value against this characteristic. */
  public boolean supports(@NonNull CharacteristicValue<MeasuredValue> value) {
    requireNonNull(value, "characteristic value cannot be null");
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) -> supports(point);
      case IntervalCharacteristicValue<MeasuredValue>(var lower, var upper) ->
          supports(lower) && supports(upper);
    };
  }
}
