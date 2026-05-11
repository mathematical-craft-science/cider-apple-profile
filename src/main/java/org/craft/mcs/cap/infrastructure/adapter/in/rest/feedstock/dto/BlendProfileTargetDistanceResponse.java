package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxis;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileDistanceMetric;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record BlendProfileTargetDistanceResponse(
    @NonNull BlendProfileTargetDistanceStatus status,
    @Nullable BigDecimal value,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull SugarAcidityCoordinatesResponse nearestTargetPoint,
    @Nullable BigDecimal tannin,
    @Nullable BigDecimal nearestTargetTannin,
    @NonNull BlendProfileObjectiveFunctionCode objectiveFunction,
    @NonNull BlendProfileDistanceMetric metric,
    @NonNull SugarAcidityTargetKind targetKind,
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @NonNull List<@NonNull BlendProfileAxis> missingMeasurements
) {
}
