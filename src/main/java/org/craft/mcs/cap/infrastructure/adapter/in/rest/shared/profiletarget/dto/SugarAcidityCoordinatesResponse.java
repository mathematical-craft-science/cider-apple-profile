package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record SugarAcidityCoordinatesResponse(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity
) {
}
