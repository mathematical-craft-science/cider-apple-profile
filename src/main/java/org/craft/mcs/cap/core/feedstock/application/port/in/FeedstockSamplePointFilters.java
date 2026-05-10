package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.application.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.Nullable;


public record FeedstockSamplePointFilters(
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable SugarAcidityZoneCriteria sugarAcidityZone,
    @Nullable ChemicalProfileCapabilityCriteria capability
) {
}
