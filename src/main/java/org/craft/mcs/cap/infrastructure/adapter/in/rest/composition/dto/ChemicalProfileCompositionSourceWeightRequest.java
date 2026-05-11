package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record ChemicalProfileCompositionSourceWeightRequest(
    @NonNull ChemicalProfileCompositionSourceWeightKind kind,
    @NonNull BigDecimal value
) {
}
