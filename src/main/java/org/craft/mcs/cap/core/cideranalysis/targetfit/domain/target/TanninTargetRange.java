package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Optional tannin interval evaluated independently alongside the mandatory sugar-acidity target. */
public record TanninTargetRange(
    @NonNull MeasuredValueRange<TanninValue> range
) {

  public TanninTargetRange {
    requireNonNull(range, "tannin target range cannot be null");
    if (range.upperBound() != MeasuredValueRange.UpperBound.INCLUSIVE) {
      throw new DomainRuleViolationException("tannin target range must include its maximum");
    }
  }

  public TanninTargetRange(
      @NonNull TanninValue min,
      @NonNull TanninValue max
  ) {
    this(MeasuredValueRange.closed(min, max));
  }

  public @NonNull TanninValue min() {
    return range.min();
  }

  public @NonNull TanninValue max() {
    return range.max();
  }

  public boolean contains(@NonNull TanninValue value) {
    return range.contains(requireNonNull(value, "tannin value cannot be null"));
  }

  public @NonNull TanninValue nearestValueTo(@NonNull TanninValue value) {
    return range.projectOntoClosure(requireNonNull(value, "tannin value cannot be null"));
  }
}
