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
      @Nullable AxisValue specificGravity,
      @Nullable AxisValue titratableAcidity,
      @Nullable AxisValue tannin
  ) {
    return of(specificGravity, titratableAcidity, tannin, null);
  }

  public static @NonNull ChemicalProfileAxes of(
      @Nullable AxisValue specificGravity,
      @Nullable AxisValue titratableAcidity,
      @Nullable AxisValue tannin,
      @Nullable AxisValue yeastAssimilableNitrogen
  ) {
    final var values = new EnumMap<ChemicalAxis, AxisValue>(ChemicalAxis.class);

    add(values, ChemicalAxis.SPECIFIC_GRAVITY, specificGravity);
    add(values, ChemicalAxis.TITRATABLE_ACIDITY, titratableAcidity);
    add(values, ChemicalAxis.TANNIN, tannin);
    add(values, ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, yeastAssimilableNitrogen);

    return new ChemicalProfileAxes(values);
  }

  private static void add(
      @NonNull Map<@NonNull ChemicalAxis, @NonNull AxisValue> values,
      @NonNull ChemicalAxis expectedAxis,
      @Nullable AxisValue value
  ) {
    if (value == null) {
      return;
    }
    if (value.axis() != expectedAxis) {
      throw new DomainRuleViolationException(
          "chemical profile axis slot %s cannot contain %s".formatted(expectedAxis, value.axis())
      );
    }
    values.put(expectedAxis, value);
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
