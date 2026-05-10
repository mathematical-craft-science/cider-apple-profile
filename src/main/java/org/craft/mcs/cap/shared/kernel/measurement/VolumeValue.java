package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Volume;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class VolumeValue
    extends MeasuredValue<Volume, VolumeValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("1000000000.000");
  private static final Unit<Volume> UNIT = BeverageUnits.LITRE;

  public VolumeValue(@NonNull Quantity<Volume> value) {
    super(value, UNIT, MIN, MAX);
  }

  public static @NonNull VolumeValue ofLiters(@NonNull BigDecimal liters) {
    requireNonNull(liters, "volume value cannot be null");
    return new VolumeValue(Quantities.getQuantity(liters, UNIT));
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
}
