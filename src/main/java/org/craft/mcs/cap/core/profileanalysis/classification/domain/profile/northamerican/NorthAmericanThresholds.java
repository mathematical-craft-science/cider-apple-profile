package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Complete, contiguous level partitions owned by the North American cider-apple profile.
 */
public record NorthAmericanThresholds(
    @NonNull ClassificationValueRange lowSugar,
    @NonNull ClassificationValueRange mediumSugar,
    @NonNull ClassificationValueRange highSugar,
    @NonNull ClassificationValueRange veryHighSugar,

    @NonNull ClassificationValueRange lowAcidity,
    @NonNull ClassificationValueRange mediumAcidity,
    @NonNull ClassificationValueRange highAcidity,
    @NonNull ClassificationValueRange veryHighAcidity,

    @NonNull ClassificationValueRange lowTannin,
    @NonNull ClassificationValueRange mediumTannin,
    @NonNull ClassificationValueRange highTannin
) {

  public NorthAmericanThresholds {
    requireNonNull(lowSugar);
    requireNonNull(mediumSugar);
    requireNonNull(highSugar);
    requireNonNull(veryHighSugar);

    requireNonNull(lowAcidity);
    requireNonNull(mediumAcidity);
    requireNonNull(highAcidity);
    requireNonNull(veryHighAcidity);

    requireNonNull(lowTannin);
    requireNonNull(mediumTannin);
    requireNonNull(highTannin);

    validatePartition(
        "North American sugar",
        List.of(lowSugar, mediumSugar, highSugar, veryHighSugar)
    );
    validatePartition(
        "North American acidity",
        List.of(lowAcidity, mediumAcidity, highAcidity, veryHighAcidity)
    );
    validatePartition(
        "North American tannin",
        List.of(lowTannin, mediumTannin, highTannin)
    );
  }

  private static void validatePartition(
      @NonNull String name,
      @NonNull List<@NonNull ClassificationValueRange> ranges
  ) {
    if (ranges.getFirst().lowerBoundary() != null || ranges.getLast().upperBoundary() != null) {
      throw new DomainRuleViolationException(name + " ranges must cover the complete value domain");
    }
    for (int index = 0; index < ranges.size() - 1; index++) {
      final var upperBoundary = ranges.get(index).upperBoundary();
      final var nextLowerBoundary = ranges.get(index + 1).lowerBoundary();
      if (upperBoundary == null
          || nextLowerBoundary == null
          || upperBoundary.compareTo(nextLowerBoundary) != 0) {
        throw new DomainRuleViolationException(name + " ranges must form a contiguous partition");
      }
    }
  }
}
