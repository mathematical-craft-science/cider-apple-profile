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
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Distance from a chemical profile point to a profile target.")
public record ProfileTargetDistanceResponse(
    @Schema(description = "Distance status.", implementation = ProfileTargetDistanceStatus.class, example = "CALCULATED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceStatus status,
    @Schema(description = "Normalized distance value. Zero means the point is inside the target.", example = "0.125", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal value,
    @Schema(description = "Whether the point is inside the sugar-acidity target geometry.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean insideSugarAcidityTarget,
    @Schema(description = "Whether the point is inside the full target, including optional tannin target.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean insideTarget,
    @Schema(description = "Nearest target point in sugar-acidity space.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityCoordinatesResponse nearestTargetPoint,
    @Schema(description = "Measured tannin, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse tannin,
    @Schema(description = "Nearest target tannin, when tannin target is configured.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse nearestTargetTannin,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Distance metric used by objective function.", implementation = ProfileDistanceMetric.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileDistanceMetric metric,
    @Schema(description = "Target kind.", implementation = SugarAcidityTargetKind.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityTargetKind targetKind,
    @Schema(description = "Sugar-acidity target geometry shape.", implementation = SugarAcidityTargetGeometryShape.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @ArraySchema(
        arraySchema = @Schema(description = "Missing profile axes.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ProfileEvaluationAxis.class)
    )
    @NonNull List<@NonNull ProfileEvaluationAxis> missingMeasurements
) {
}
