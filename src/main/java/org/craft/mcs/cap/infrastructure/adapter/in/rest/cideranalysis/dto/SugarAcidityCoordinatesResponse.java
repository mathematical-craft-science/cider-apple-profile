package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record SugarAcidityCoordinatesResponse(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity
) {
}
