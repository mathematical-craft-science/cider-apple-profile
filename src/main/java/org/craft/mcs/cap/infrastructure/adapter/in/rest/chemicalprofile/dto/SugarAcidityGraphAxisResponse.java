package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;


public record SugarAcidityGraphAxisResponse(
    @NonNull String code,
    @NonNull String label,
    @NonNull String unit,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max,
    @NonNull List<@NonNull BigDecimal> thresholdLines
) {
}
