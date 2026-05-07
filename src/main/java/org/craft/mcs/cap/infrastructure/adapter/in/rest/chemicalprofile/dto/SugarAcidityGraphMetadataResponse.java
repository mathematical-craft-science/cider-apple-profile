package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.util.List;
import org.jspecify.annotations.NonNull;


public record SugarAcidityGraphMetadataResponse(
    @NonNull SugarAcidityGraphAxisResponse sugarAxis,
    @NonNull SugarAcidityGraphAxisResponse acidityAxis,
    @NonNull List<@NonNull SugarAcidityGraphZoneResponse> zones
) {
}
