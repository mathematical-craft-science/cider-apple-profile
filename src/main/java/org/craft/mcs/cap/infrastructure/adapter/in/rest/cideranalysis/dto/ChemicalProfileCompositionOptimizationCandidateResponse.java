package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ChemicalProfileCompositionOptimizationCandidateResponse(
    @NonNull List<@NonNull ChemicalProfileCompositionSourceResponse> sources,
    @NonNull SugarAcidityProfilePointResponse profile,
    @Nullable BigDecimal totalVolumeLiters,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @NonNull ProfileTargetDistanceResponse targetDistance
) {
}
