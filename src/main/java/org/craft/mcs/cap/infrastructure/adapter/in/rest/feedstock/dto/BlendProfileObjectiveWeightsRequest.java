package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


public record BlendProfileObjectiveWeightsRequest(
    @Nullable BigDecimal sugar,
    @Nullable BigDecimal acidity,
    @Nullable BigDecimal tannin
) {
}
