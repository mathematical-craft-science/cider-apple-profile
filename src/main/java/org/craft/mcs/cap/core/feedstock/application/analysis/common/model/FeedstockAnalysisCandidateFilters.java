package org.craft.mcs.cap.core.feedstock.application.analysis.common.model;

import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.jspecify.annotations.Nullable;


/**
 * Shared filters for independent feedstock analysis scenarios.
 *
 * <p>Batch and season criteria narrow source evidence. Zone and capability criteria narrow the
 * derived assessment after that evidence has been evaluated.</p>
 */
public record FeedstockAnalysisCandidateFilters(
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable JolicoeurSugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ProfileAnalysisCapabilityCriteria capability
) {
}
