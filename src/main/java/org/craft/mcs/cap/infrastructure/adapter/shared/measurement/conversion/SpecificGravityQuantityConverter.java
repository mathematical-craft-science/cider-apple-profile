package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import javax.measure.Quantity;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Brix;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Plato;
import org.craft.mcs.cap.shared.kernel.measurement.unit.SpecificGravity;
import org.craft.mcs.cap.shared.kernel.sugar.BrixValue;
import org.craft.mcs.cap.shared.kernel.sugar.PlatoValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.converter.BuiltInSugarConversions;
import org.craft.mcs.cap.shared.kernel.sugar.converter.SugarConversionService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Converts SG and related sugar-scale quantities into canonical Specific Gravity.
 *
 * <p>Brix and Plato are scale conversions, so they use the sugar conversion service after being
 * represented as JSR-385 quantities at the inbound boundary.</p>
 */
public final class SpecificGravityQuantityConverter
    implements ExternalMeasurementConverter<SpecificGravityValue> {

  private static final @NonNull String CHARACTERISTIC = "specific gravity";
  private static final @NonNull String CANONICAL_UNIT = BeverageUnits.label(BeverageUnits.SG);
  private static final @NonNull SugarConversionService SUGAR_CONVERSION_SERVICE =
      BuiltInSugarConversions.defaultService();

  @Override
  public @NonNull SpecificGravityValue convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "specific gravity value cannot be null");

    return switch (ExternalMeasurementUnits.unitKey(unit, CANONICAL_UNIT)) {
      case "sg", "specific gravity" -> new SpecificGravityValue(specificGravityQuantity(value));
      case "brix", "°bx", "deg brix" -> SUGAR_CONVERSION_SERVICE.convert(
          new BrixValue(brixQuantity(value)),
          SpecificGravityValue.class
      );
      case "plato", "°p", "deg plato" -> SUGAR_CONVERSION_SERVICE.convert(
          new PlatoValue(platoQuantity(value)),
          SpecificGravityValue.class
      );
      default -> throw new UnsupportedMeasurementUnitException(CHARACTERISTIC, unit);
    };
  }

  private static @NonNull Quantity<SpecificGravity> specificGravityQuantity(@NonNull BigDecimal value) {
    return Quantities.getQuantity(value, BeverageUnits.SG);
  }

  private static @NonNull Quantity<Brix> brixQuantity(@NonNull BigDecimal value) {
    return Quantities.getQuantity(value, BeverageUnits.BRIX);
  }

  private static @NonNull Quantity<Plato> platoQuantity(@NonNull BigDecimal value) {
    return Quantities.getQuantity(value, BeverageUnits.PLATO);
  }
}
