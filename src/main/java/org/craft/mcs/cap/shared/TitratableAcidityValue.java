package org.craft.mcs.cap.shared;

import java.math.BigDecimal;
import java.util.Optional;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.model.TitratableAcidity;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;

public record TitratableAcidityValue(
    @NonNull Quantity<TitratableAcidity> value
) implements Comparable<TitratableAcidityValue> {

  private static final BigDecimal MIN_TA = BigDecimal.ZERO;

  /**
   * Practical upper bound for beverages/wort/sour beer ranges.
   * Adjust if your domain requires higher acidity.
   */
  private static final BigDecimal MAX_TA = new BigDecimal("30.0");

  private static final Unit<TitratableAcidity> UNIT =
      BeverageUnits.GRAMS_PER_LITER_AS_LACTIC_ACID;

  public TitratableAcidityValue {
    requireNonNull(value, "titratable acidity quantity cannot be null");

    if (!UNIT.equals(value.getUnit())) {
      throw new IllegalArgumentException(
          "Unit mismatch. Expected " + UNIT + ", got " + value.getUnit()
      );
    }

    value = normalizeQuantity(value);
    validateRange((BigDecimal) value.getValue());
  }

  public static TitratableAcidityValue of(@NonNull BigDecimal gramsPerLiter) {
    requireNonNull(gramsPerLiter, "titratable acidity value cannot be null");
    return new TitratableAcidityValue(Quantities.getQuantity(gramsPerLiter, UNIT));
  }

  public static TitratableAcidityValue of(@NonNull String gramsPerLiter) {
    requireNonNull(gramsPerLiter, "titratable acidity value cannot be null");
    return of(new BigDecimal(gramsPerLiter));
  }

  public static TitratableAcidityValue of(double gramsPerLiter) {
    return of(BigDecimal.valueOf(gramsPerLiter));
  }

  public BigDecimal asBigDecimal() {
    return (BigDecimal) value.getValue();
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  public Unit<TitratableAcidity> unit() {
    return UNIT;
  }

  @Override
  public int compareTo(@NonNull TitratableAcidityValue other) {
    requireNonNull(other, "other titratable acidity value cannot be null");
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }

  private static void validateRange(BigDecimal acidity) {
    if (acidity.compareTo(MIN_TA) < 0 || acidity.compareTo(MAX_TA) > 0) {
      throw new IllegalArgumentException(
          "Titratable acidity value " + acidity
              + " out of range [" + MIN_TA + ", " + MAX_TA + "] g/L"
      );
    }
  }

  private static Quantity<TitratableAcidity> normalizeQuantity(
      Quantity<TitratableAcidity> quantity
  ) {
    BigDecimal normalized = Optional.of(quantity.getValue())
        .filter(BigDecimal.class::isInstance)
        .map(BigDecimal.class::cast)
        .orElseGet(() -> toBigDecimal(quantity.getValue()));

    return Quantities.getQuantity(normalized, UNIT);
  }

  private static BigDecimal toBigDecimal(Number number) {
    return switch (number) {
      case BigDecimal bd -> bd;
      case Byte b -> BigDecimal.valueOf(b.longValue());
      case Short s -> BigDecimal.valueOf(s.longValue());
      case Integer i -> BigDecimal.valueOf(i.longValue());
      case Long l -> BigDecimal.valueOf(l);
      case Float f -> BigDecimal.valueOf(f.doubleValue());
      case Double d -> BigDecimal.valueOf(d);
      default -> new BigDecimal(number.toString());
    };
  }
}
