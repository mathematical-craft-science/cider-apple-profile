package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model;

import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


/**
 * Analysis-owned filters applied before a catalog profile is interpreted by a concrete scenario.
 *
 * <p>Derived graph or target criteria belong to the concrete scenario query and are deliberately
 * excluded from this source selection.</p>
 */
public record AppleVarietyAnalysisFilters(
    @Nullable TextSearchCriteria nameContains
) {
}
