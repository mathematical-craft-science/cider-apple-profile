package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ChemicalProfileCompositionSourceResponse(
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Nullable BigDecimal requestedVolumeLiters,
    @Nullable BigDecimal proportion
) {
}
