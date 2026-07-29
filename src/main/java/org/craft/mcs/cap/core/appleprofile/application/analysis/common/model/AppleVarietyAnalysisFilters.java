package org.craft.mcs.cap.core.appleprofile.application.analysis.common.model;

import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


/**
 * Shared catalog filters applied before an AppleVariety is interpreted by a concrete analysis scenario.
 */
public record AppleVarietyAnalysisFilters(
    @Nullable TextSearchCriteria nameContains,
    @Nullable JolicoeurSugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ProfileAnalysisCapabilityCriteria capability
) {
}
