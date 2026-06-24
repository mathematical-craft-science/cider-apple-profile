package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietyPointFilters(
    @Nullable TextSearchCriteria nameContains,
    @Nullable SugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ChemicalProfileCapabilityCriteria capability
) {
}
