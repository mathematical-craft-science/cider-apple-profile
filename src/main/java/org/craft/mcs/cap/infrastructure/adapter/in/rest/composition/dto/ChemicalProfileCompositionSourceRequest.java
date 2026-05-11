package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ChemicalProfileCompositionSourceRequest(
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @NonNull ChemicalProfileCompositionAxesRequest axes,
    @NonNull ChemicalProfileCompositionSourceWeightRequest weight,
    @Nullable BigDecimal availableVolumeLiters
) {
}
