package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record SugarAcidityGraphRangeResponse(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {
}
