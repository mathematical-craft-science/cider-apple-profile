package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


public record ProfileTargetObjectiveWeightsRequest(
    @Nullable BigDecimal sugar,
    @Nullable BigDecimal acidity,
    @Nullable BigDecimal tannin
) {
}
