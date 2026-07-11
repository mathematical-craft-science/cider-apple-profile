package org.craft.mcs.cap.shared.kernel.measurement.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Volume;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Value object for material volume in CAP canonical volume units.
 *
 * <p>Volume is represented in liters and is used by feedstock, production, composition, and
 * traceability accounting. Keeping the accepted range here gives adapters a single source for
 * metadata while preserving volume validation in the shared kernel.
 */
@ValueObject
public final class VolumeValue
    extends MeasuredValue<Volume, VolumeValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("1000000000.000");
  private static final BigDecimal STEP = new BigDecimal("0.001");
  private static final Unit<Volume> UNIT = BeverageUnits.LITRE;

  public VolumeValue(@NonNull Quantity<Volume> value) {
    super(value, UNIT, MIN, MAX);
  }

  public static @NonNull VolumeValue ofLiters(@NonNull BigDecimal liters) {
    requireNonNull(liters, "volume value cannot be null");
    return new VolumeValue(Quantities.getQuantity(liters, UNIT));
  }

  public static @NonNull BigDecimal minValue() {
    return MIN;
  }

  public static @NonNull BigDecimal maxValue() {
    return MAX;
  }

  public static @NonNull BigDecimal stepValue() {
    return STEP;
  }

  public static @NonNull VolumeValue ofLiters(@NonNull String liters) {
    requireNonNull(liters, "volume value cannot be null");
    return ofLiters(new BigDecimal(liters));
  }

  public static @NonNull VolumeValue ofLiters(double liters) {
    return ofLiters(BigDecimal.valueOf(liters));
  }

  public @NonNull BigDecimal liters() {
    return asBigDecimal();
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  public boolean isZero() {
    return asBigDecimal().signum() == 0;
  }

  public @NonNull VolumeValue subtract(@NonNull VolumeValue other) {
    requireNonNull(other, "volume value to subtract cannot be null");
    return ofLiters(liters().subtract(other.liters()));
  }
}
