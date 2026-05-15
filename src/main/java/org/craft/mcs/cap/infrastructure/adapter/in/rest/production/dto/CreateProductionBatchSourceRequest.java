package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.NonNull;


public record CreateProductionBatchSourceRequest(
    @NonNull UUID feedstockBatchId,
    @NonNull BigDecimal volumeLiters
) {
}
