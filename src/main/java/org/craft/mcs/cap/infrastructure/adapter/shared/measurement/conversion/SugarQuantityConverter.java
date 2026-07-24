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
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.sugar.converter.BuiltInSugarConversions;
import org.craft.mcs.cap.shared.kernel.sugar.converter.SugarConversionService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Converts external sugar scale quantities into canonical specific gravity.
 * Brix and Plato are scale conversions, so they use the domain sugar conversion service
 * after being represented as JSR-385 quantities at the REST boundary.
 */
public final class SugarQuantityConverter
    implements ExternalMeasurementConverter<SugarConcentration> {

  private static final @NonNull String AXIS = "sugar";
  private static final @NonNull String CANONICAL_UNIT = BeverageUnits.label(BeverageUnits.SG);
  private static final @NonNull SugarConversionService SUGAR_CONVERSION_SERVICE =
      BuiltInSugarConversions.defaultService();

  @Override
  public @NonNull SugarConcentration convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return SugarConcentration.ofSpecificGravity(convertSpecificGravity(value, unit));
  }

  private static @NonNull SpecificGravityValue convertSpecificGravity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "sugar value cannot be null");

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
      default -> throw new UnsupportedMeasurementUnitException(AXIS, unit);
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
