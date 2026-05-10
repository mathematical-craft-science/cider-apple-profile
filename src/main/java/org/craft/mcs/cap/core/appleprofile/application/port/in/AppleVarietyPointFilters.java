package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.shared.application.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietyPointFilters(
    @Nullable TextSearchCriteria nameContains,
    @Nullable SugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ChemicalProfileCapabilityCriteria capability
) {
}
