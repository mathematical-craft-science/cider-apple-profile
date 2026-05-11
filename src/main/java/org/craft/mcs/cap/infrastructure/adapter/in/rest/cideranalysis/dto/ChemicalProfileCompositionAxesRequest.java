package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ChemicalProfileCompositionAxesRequest(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity,
    @Nullable BigDecimal tannin
) {
}
