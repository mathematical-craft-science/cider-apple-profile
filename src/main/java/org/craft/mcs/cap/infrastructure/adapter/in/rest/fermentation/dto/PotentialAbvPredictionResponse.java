package org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record PotentialAbvPredictionResponse(
    @NonNull BigDecimal originalGravity,
    @NonNull BigDecimal assumedFinalGravity,
    @NonNull BigDecimal potentialAbv,
    @NonNull String method
) {
}
