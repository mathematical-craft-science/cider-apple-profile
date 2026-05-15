package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ProductionBatchCompositionResponse(
    @NonNull List<@NonNull ProductionBatchSourceResponse> sources,
    @NonNull SugarAcidityProfilePointResponse profile,
    @NonNull BigDecimal totalVolumeLiters,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistanceResponse targetDistance
) {
}
