package org.craft.mcs.cap.shared.domain.classification.rule;

import java.util.EnumMap;
import java.util.Map;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ClassificationRule<C extends Enum<C>> {

  private final @NonNull C appleClass;
  private final @NonNull Map<@NonNull ChemicalAxis, @NonNull AxisRange> ranges;

  private ClassificationRule(
      @NonNull C appleClass,
      @NonNull Map<@NonNull ChemicalAxis, @NonNull AxisRange> ranges
  ) {
    this.appleClass = requireNonNull(appleClass, "classification class cannot be null");
    this.ranges = Map.copyOf(requireNonNull(ranges, "classification ranges cannot be null"));
  }

  public static <C extends Enum<C>> @NonNull ClassificationRule<C> of(
      @NonNull C appleClass,
      @Nullable AxisRange sugar,
      @Nullable AxisRange acidity,
      @Nullable AxisRange tanninity
  ) {
    final var ranges = new EnumMap<ChemicalAxis, AxisRange>(ChemicalAxis.class);

    if (sugar != null) {
      ranges.put(ChemicalAxis.SUGAR, sugar);
    }
    if (acidity != null) {
      ranges.put(ChemicalAxis.ACIDITY, acidity);
    }
    if (tanninity != null) {
      ranges.put(ChemicalAxis.TANNINITY, tanninity);
    }

    return new ClassificationRule<>(appleClass, ranges);
  }

  public @NonNull C appleClass() {
    return appleClass;
  }

  public boolean matches(@NonNull ChemicalProfileAxes axes) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    for (final var entry : ranges.entrySet()) {
      final var axisValue = axes.valueOf(entry.getKey());
      if (axisValue.isEmpty()) {
        return false;
      }
      if (!entry.getValue().contains(axisValue.orElseThrow().value())) {
        return false;
      }
    }

    return true;
  }

  public @NonNull Map<@NonNull ChemicalAxis, @NonNull AxisRange> ranges() {
    return ranges;
  }
}
