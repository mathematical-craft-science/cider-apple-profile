package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.northamerican;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule
    .ClassificationValuePartitions.requireComplete;


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

    requireComplete(
        "North American sugar",
        List.of(lowSugar, mediumSugar, highSugar, veryHighSugar)
    );
    requireComplete(
        "North American acidity",
        List.of(lowAcidity, mediumAcidity, highAcidity, veryHighAcidity)
    );
    requireComplete(
        "North American tannin",
        List.of(lowTannin, mediumTannin, highTannin)
    );
  }

}
