package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.Nullable;


public record AppleVarietySugarAcidityZonePointQueryRequest(
    @Nullable String nameContains,
    @Nullable SugarAcidityZone zone,
    @Nullable ChemicalProfileCapability capability,
    @Nullable Boolean capable,
    @Nullable String after,
    @Nullable Integer limit
) {
}
