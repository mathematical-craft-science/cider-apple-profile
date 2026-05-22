package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileAxes {

  private final @NonNull Map<@NonNull ChemicalAxis, @NonNull AxisValue> values;

  private ChemicalProfileAxes(@NonNull Map<@NonNull ChemicalAxis, @NonNull AxisValue> values) {
    this.values = Map.copyOf(requireNonNull(values, "chemical axis values cannot be null"));
  }

  public static @NonNull ChemicalProfileAxes of(
      @Nullable AxisValue sugar,
      @Nullable AxisValue acidity,
      @Nullable AxisValue tanninity
  ) {
    return of(sugar, acidity, tanninity, null);
  }

  public static @NonNull ChemicalProfileAxes of(
      @Nullable AxisValue sugar,
      @Nullable AxisValue acidity,
      @Nullable AxisValue tanninity,
      @Nullable AxisValue yeastAssimilableNitrogen
  ) {
    final var values = new EnumMap<ChemicalAxis, AxisValue>(ChemicalAxis.class);

    if (sugar != null) {
      values.put(ChemicalAxis.SUGAR, sugar);
    }
    if (acidity != null) {
      values.put(ChemicalAxis.ACIDITY, acidity);
    }
    if (tanninity != null) {
      values.put(ChemicalAxis.TANNINITY, tanninity);
    }
    if (yeastAssimilableNitrogen != null) {
      values.put(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, yeastAssimilableNitrogen);
    }

    return new ChemicalProfileAxes(values);
  }

  public @NonNull Optional<@NonNull AxisValue> valueOf(@NonNull ChemicalAxis axis) {
    requireNonNull(axis, "chemical axis cannot be null");
    return Optional.ofNullable(values.get(axis));
  }

  public boolean has(@NonNull ChemicalAxis axis) {
    requireNonNull(axis, "chemical axis cannot be null");
    return values.containsKey(axis);
  }
}
