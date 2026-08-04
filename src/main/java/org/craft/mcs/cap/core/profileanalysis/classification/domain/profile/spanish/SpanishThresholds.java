package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.spanish;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;


public record SpanishThresholds(
    @NonNull ClassificationValueRange lowAcid,
    @NonNull ClassificationValueRange mediumAcid,
    @NonNull ClassificationValueRange highAcid,
    @NonNull ClassificationValueRange lowTannin,
    @NonNull ClassificationValueRange mediumTannin,
    @NonNull ClassificationValueRange highTannin
) {
}
