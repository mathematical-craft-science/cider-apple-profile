package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import org.craft.mcs.cap.shared.domain.chemical.ConfidenceLevel;
import org.jspecify.annotations.Nullable;


public record ChemicalProfileRequest(
    @Nullable ChemicalMeasurementRequest sugar,
    @Nullable ChemicalMeasurementRequest ph,
    @Nullable ChemicalMeasurementRequest titratableAcidity,
    @Nullable ChemicalMeasurementRequest tanninity,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence
) {
}
