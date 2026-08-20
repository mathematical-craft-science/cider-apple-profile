package org.craft.mcs.cap.core.cideranalysis.classification.domain.rule;

import java.util.EnumMap;
import java.util.Map;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.ClassificationIntervalPolicy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ClassificationRule<C extends Enum<C>> {

  private final @NonNull C appleClass;
  private final @NonNull Map<@NonNull ClassificationCharacteristic, @NonNull ClassificationValueRange> ranges;

  private ClassificationRule(
      @NonNull C appleClass,
      @NonNull Map<@NonNull ClassificationCharacteristic, @NonNull ClassificationValueRange> ranges
  ) {
    this.appleClass = requireNonNull(appleClass, "classification class cannot be null");
    this.ranges = Map.copyOf(requireNonNull(ranges, "classification ranges cannot be null"));
  }

  public static <C extends Enum<C>> @NonNull ClassificationRule<C> of(
      @NonNull C appleClass,
      @Nullable ClassificationValueRange sugarContent,
      @Nullable ClassificationValueRange acidity,
      @Nullable ClassificationValueRange tannin
  ) {
    final var ranges = new EnumMap<ClassificationCharacteristic, ClassificationValueRange>(
        ClassificationCharacteristic.class
    );

    if (sugarContent != null) {
      ranges.put(ClassificationCharacteristic.SUGAR_CONTENT, sugarContent);
    }
    if (acidity != null) {
      ranges.put(ClassificationCharacteristic.ACIDITY, acidity);
    }
    if (tannin != null) {
      ranges.put(ClassificationCharacteristic.TANNIN, tannin);
    }

    return new ClassificationRule<>(appleClass, ranges);
  }

  public @NonNull C appleClass() {
    return appleClass;
  }

  public boolean matches(
      @NonNull CiderAppleClassificationInput input,
      @NonNull ClassificationIntervalPolicy intervalPolicy
  ) {
    requireNonNull(input, "cider apple classification input cannot be null");
    requireNonNull(intervalPolicy, "classification interval policy cannot be null");

    for (final var entry : ranges.entrySet()) {
      if (!input.has(entry.getKey())) {
        return false;
      }
      if (!entry.getValue().contains(input.representativeValue(entry.getKey(), intervalPolicy))) {
        return false;
      }
    }

    return true;
  }

  public @NonNull Map<@NonNull ClassificationCharacteristic, @NonNull ClassificationValueRange> ranges() {
    return ranges;
  }
}
