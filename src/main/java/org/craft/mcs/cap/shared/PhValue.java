package org.craft.mcs.cap.shared;


import java.math.BigDecimal;
import java.util.Optional;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.jspecify.annotations.NonNull;
import systems.uom.quantity.Acidity;
import systems.uom.ucum.UCUM;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;

public record PhValue(
    @NonNull Quantity<Acidity> value
) implements Comparable<PhValue> {

  private static final BigDecimal MIN_PH = BigDecimal.ZERO;
  private static final BigDecimal MAX_PH = new BigDecimal("15.0");

  private static final Unit<Acidity> UNIT = UCUM.PH;

  public PhValue {
    requireNonNull(value, "pH quantity cannot be null");

    if (!UNIT.equals(value.getUnit())) {
      throw new IllegalArgumentException(
          "Unit mismatch. Expected " + UNIT + ", got " + value.getUnit()
      );
    }

    value = normalizeQuantity(value);
    validateRange((BigDecimal) value.getValue());
  }

  public static PhValue of(@NonNull BigDecimal ph) {
    requireNonNull(ph, "pH value cannot be null");
    return new PhValue(Quantities.getQuantity(ph, UNIT));
  }

  public static PhValue of(@NonNull String ph) {
    requireNonNull(ph, "pH value cannot be null");
    return of(new BigDecimal(ph));
  }

  public static PhValue of(double ph) {
    return of(BigDecimal.valueOf(ph));
  }

  private static void validateRange(BigDecimal ph) {
    if (ph.compareTo(MIN_PH) < 0 || ph.compareTo(MAX_PH) > 0) {
      throw new IllegalArgumentException(
          "pH value " + ph + " out of range [" + MIN_PH + ", " + MAX_PH + "]"
      );
    }
  }

  private static Quantity<Acidity> normalizeQuantity(Quantity<Acidity> quantity) {
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

  public BigDecimal asBigDecimal() {
    return (BigDecimal) value.getValue();
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  public Unit<Acidity> unit() {
    return UNIT;
  }

  @Override
  public int compareTo(@NonNull PhValue other) {
    requireNonNull(other, "other pH value cannot be null");
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }
}