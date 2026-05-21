package org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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


@Schema(description = "Distance from a chemical profile point to a profile target.")
public record ProfileTargetDistanceResponse(
    @Schema(description = "Distance status.", implementation = ProfileTargetDistanceStatus.class, example = "CALCULATED")
    @NonNull ProfileTargetDistanceStatus status,
    @Schema(description = "Normalized distance value. Zero means the point is inside the target.", example = "0.125")
    @Nullable BigDecimal value,
    @Schema(description = "Whether the point is inside the sugar-acidity target geometry.", example = "true")
    boolean insideSugarAcidityTarget,
    @Schema(description = "Whether the point is inside the full target, including optional tannin target.", example = "true")
    boolean insideTarget,
    @Schema(description = "Nearest target point in sugar-acidity space.")
    @NonNull SugarAcidityCoordinatesResponse nearestTargetPoint,
    @Schema(description = "Measured tannin in mg/L TAE, when available.", example = "1200")
    @Nullable BigDecimal tannin,
    @Schema(description = "Nearest target tannin in mg/L TAE, when tannin target is configured.", example = "1200")
    @Nullable BigDecimal nearestTargetTannin,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class)
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Distance metric used by objective function.", implementation = ProfileDistanceMetric.class)
    @NonNull ProfileDistanceMetric metric,
    @Schema(description = "Target kind.", implementation = SugarAcidityTargetKind.class)
    @NonNull SugarAcidityTargetKind targetKind,
    @Schema(description = "Sugar-acidity target geometry shape.", implementation = SugarAcidityTargetGeometryShape.class)
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @ArraySchema(schema = @Schema(description = "Missing profile axis.", implementation = ProfileEvaluationAxis.class))
    @NonNull List<@NonNull ProfileEvaluationAxis> missingMeasurements
) {
}
