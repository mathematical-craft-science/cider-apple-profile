package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
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
      @Nullable AxisValue tannin
  ) {
    return of(sugar, acidity, tannin, null);
  }

  public static @NonNull ChemicalProfileAxes of(
      @Nullable AxisValue sugar,
      @Nullable AxisValue acidity,
      @Nullable AxisValue tannin,
      @Nullable AxisValue yeastAssimilableNitrogen
  ) {
    final var values = new EnumMap<ChemicalAxis, AxisValue>(ChemicalAxis.class);

    if (sugar != null) {
      values.put(ChemicalAxis.SUGAR, sugar);
    }
    if (acidity != null) {
      values.put(ChemicalAxis.ACIDITY, acidity);
    }
    if (tannin != null) {
      values.put(ChemicalAxis.TANNIN, tannin);
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

  /**
   * Returns the value of an axis required by the current domain capability.
   *
   * @param axis required chemical axis
   * @return value associated with the axis
   * @throws DomainRuleViolationException when the required axis is absent
   */
  public @NonNull AxisValue requireValueOf(@NonNull ChemicalAxis axis) {
    requireNonNull(axis, "chemical axis cannot be null");
    final var value = values.get(axis);
    if (value == null) {
      throw new DomainRuleViolationException("required chemical axis is missing: " + axis);
    }
    return value;
  }

  public boolean has(@NonNull ChemicalAxis axis) {
    requireNonNull(axis, "chemical axis cannot be null");
    return values.containsKey(axis);
  }
}
