package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import org.jspecify.annotations.NonNull;


public record ChemicalProfileCompositionSourceReferenceRequest(
    @NonNull String type,
    @NonNull String id
) {
}
