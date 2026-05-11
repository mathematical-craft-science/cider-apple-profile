package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileDistanceMetric;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record ProfileTargetDistanceResponse(
    @NonNull ProfileTargetDistanceStatus status,
    @Nullable BigDecimal value,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull SugarAcidityCoordinatesResponse nearestTargetPoint,
    @Nullable BigDecimal tannin,
    @Nullable BigDecimal nearestTargetTannin,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @NonNull ProfileDistanceMetric metric,
    @NonNull SugarAcidityTargetKind targetKind,
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @NonNull List<@NonNull ProfileEvaluationAxis> missingMeasurements
) {
}
