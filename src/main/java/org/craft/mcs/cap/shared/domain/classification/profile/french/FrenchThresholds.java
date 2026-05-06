package org.craft.mcs.cap.shared.domain.classification.profile.french;

import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.jspecify.annotations.NonNull;


public record FrenchThresholds(
    @NonNull AxisRange lowAcid,
    @NonNull AxisRange mediumAcid,
    @NonNull AxisRange highAcid,
    @NonNull AxisRange lowTannin,
    @NonNull AxisRange mediumTannin,
    @NonNull AxisRange highTannin
) {
}
