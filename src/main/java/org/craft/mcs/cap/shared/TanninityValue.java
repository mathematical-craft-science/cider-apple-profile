package org.craft.mcs.cap.shared;

import java.math.BigDecimal;
import java.util.Optional;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.model.Tanninity;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Tannin / polyphenol content for cider.
 * <p>
 * Canonical unit: mg/L as tannic acid equivalents (TAE)
 * <p>
 * Domain range: 0 .. 3000 mg/L
 */
public record TanninityValue(@NonNull Quantity<Tanninity> value)
    implements Comparable<TanninityValue> {

  private static final BigDecimal MIN_TANNINITY = BigDecimal.ZERO;
  /**
   * Practical upper bound for cider.   * Can be adjusted later if domain expands.
   */
  private static final BigDecimal MAX_TANNINITY = new BigDecimal("3000");
  private static final Unit<Tanninity> UNIT = BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID;

  public TanninityValue {
    requireNonNull(value, "Tanninity quantity cannot be null");
    if (!UNIT.equals(value.getUnit())) {
      throw new IllegalArgumentException("Unit mismatch. Expected " + UNIT + ", got " + value.getUnit());
    }
    value = normalizeQuantity(value);
    validateRange((BigDecimal) value.getValue());
  }

  public static TanninityValue of(@NonNull BigDecimal mgPerLiter) {
    requireNonNull(mgPerLiter, "Tanninity value cannot be null");
    return new TanninityValue(Quantities.getQuantity(mgPerLiter, UNIT));
  }

  public static TanninityValue of(@NonNull String mgPerLiter) {
    requireNonNull(mgPerLiter, "Tanninity value cannot be null");
    return of(new BigDecimal(mgPerLiter));
  }

  public static TanninityValue of(double mgPerLiter) {
    return of(BigDecimal.valueOf(mgPerLiter));
  }

  private static void validateRange(BigDecimal tanninity) {
    if (tanninity.compareTo(MIN_TANNINITY) < 0 || tanninity.compareTo(MAX_TANNINITY) > 0) {
      throw new IllegalArgumentException(
          "Tanninity value " + tanninity + " out of range [" + MIN_TANNINITY + ", " + MAX_TANNINITY + "] mg/L TAE");
    }
  }

  private static Quantity<Tanninity> normalizeQuantity(Quantity<Tanninity> quantity) {
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

  public Unit<Tanninity> unit() {
    return UNIT;
  }

  @Override
  public int compareTo(@NonNull TanninityValue other) {
    requireNonNull(other, "Other tanninity value cannot be null");
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }

  @Override
  public String toString() {
    return asBigDecimal().stripTrailingZeros().toPlainString() + " mg/L TAE";
  }
}
