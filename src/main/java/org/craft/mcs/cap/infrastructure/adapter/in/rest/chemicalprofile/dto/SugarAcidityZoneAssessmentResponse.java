package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record SugarAcidityZoneAssessmentResponse(
    boolean classified,
    @Nullable String zone,
    @NonNull String sugar,
    @NonNull String acidity,
    @Nullable String tannin,
    boolean balancedForSingleVariety,
    @NonNull SugarAcidityProfilePointResponse point
) {
}
