package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FeedstockBatchCompositionOptimizationCandidateResponse(
    @NonNull List<@NonNull FeedstockBatchCompositionOptimizationSourceResponse> sources,
    @NonNull SugarAcidityProfilePointResponse profile,
    @Nullable BigDecimal totalVolumeLiters,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @NonNull ProfileTargetDistanceResponse targetDistance
) {
}
