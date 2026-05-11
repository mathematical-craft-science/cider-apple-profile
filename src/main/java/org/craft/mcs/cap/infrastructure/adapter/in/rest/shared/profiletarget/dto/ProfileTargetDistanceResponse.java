package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileDistanceMetric;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetKind;
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
