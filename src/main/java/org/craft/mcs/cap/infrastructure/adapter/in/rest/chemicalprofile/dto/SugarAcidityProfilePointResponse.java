package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record SugarAcidityProfilePointResponse(
    @NonNull SugarAcidityAxisPointResponse sugar,
    @NonNull SugarAcidityAxisPointResponse acidity,
    @Nullable SugarAcidityAxisPointResponse tannin
) {
}
