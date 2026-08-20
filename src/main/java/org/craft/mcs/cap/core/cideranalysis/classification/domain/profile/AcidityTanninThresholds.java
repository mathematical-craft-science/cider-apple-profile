package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule
    .ClassificationValuePartitions.requireComplete;


/**
 * Three-level acidity and tannin ranges used by a cider-apple classification profile.
 *
 * <p>The owning strategy supplies the classification-system semantics and the built-in threshold values.</p>
 */
public record AcidityTanninThresholds(
    @NonNull ClassificationValueRange lowAcid,
    @NonNull ClassificationValueRange mediumAcid,
    @NonNull ClassificationValueRange highAcid,
    @NonNull ClassificationValueRange lowTannin,
    @NonNull ClassificationValueRange mediumTannin,
    @NonNull ClassificationValueRange highTannin
) {

  public AcidityTanninThresholds {
    requireNonNull(lowAcid, "low acid range cannot be null");
    requireNonNull(mediumAcid, "medium acid range cannot be null");
    requireNonNull(highAcid, "high acid range cannot be null");
    requireNonNull(lowTannin, "low tannin range cannot be null");
    requireNonNull(mediumTannin, "medium tannin range cannot be null");
    requireNonNull(highTannin, "high tannin range cannot be null");
    requireComplete("acidity", List.of(lowAcid, mediumAcid, highAcid));
    requireComplete("tannin", List.of(lowTannin, mediumTannin, highTannin));
  }
}
