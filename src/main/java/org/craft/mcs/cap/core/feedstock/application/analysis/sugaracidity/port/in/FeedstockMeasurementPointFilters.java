package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.jspecify.annotations.Nullable;


public record FeedstockMeasurementPointFilters(
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable SugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ChemicalProfileCapabilityCriteria capability
) {
}
