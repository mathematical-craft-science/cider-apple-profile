package org.craft.mcs.cap.core.cideranalysis.classification.domain.rule;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Validates ordered ranges that must cover one complete classification axis without gaps or overlaps. */
public final class ClassificationValuePartitions {

  private ClassificationValuePartitions() {
  }

  public static void requireComplete(
      @NonNull String name,
      @NonNull List<@NonNull ClassificationValueRange> ranges
  ) {
    requireNonNull(name, "classification partition name cannot be null");
    final var immutableRanges = List.copyOf(requireNonNull(ranges, "classification ranges cannot be null"));
    if (immutableRanges.size() < 2) {
      throw new DomainRuleViolationException(name + " ranges must contain at least two segments");
    }
    if (immutableRanges.getFirst().lowerBoundary() != null
        || immutableRanges.getLast().upperBoundary() != null) {
      throw new DomainRuleViolationException(name + " ranges must cover the complete value domain");
    }
    for (int index = 0; index < immutableRanges.size() - 1; index++) {
      final var upperBoundary = immutableRanges.get(index).upperBoundary();
      final var nextLowerBoundary = immutableRanges.get(index + 1).lowerBoundary();
      if (upperBoundary == null
          || nextLowerBoundary == null
          || upperBoundary.compareTo(nextLowerBoundary) != 0) {
        throw new DomainRuleViolationException(name + " ranges must form a contiguous partition");
      }
    }
  }
}
