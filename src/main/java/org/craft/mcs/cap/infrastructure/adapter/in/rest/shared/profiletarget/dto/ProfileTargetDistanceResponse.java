package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Distance from a typed characteristic point to a profile target.")
public record ProfileTargetDistanceResponse(
    @Schema(
        description = "Distance status.",
        implementation = ProfileTargetDistanceStatus.class,
        example = "SCORED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetDistanceStatus status,
    @Schema(
        description = "Technical non-negative distance metric. Lower is closer; its scale depends on the selected "
            + "normalizer and metric.",
        example = "0.125",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal value,
    @Schema(
        description = "Authoritative target match from 0 to 100 within the selected scoring configuration; "
            + "100 is reserved for a point inside the full target; absent when fit is unscorable.",
        minimum = "0",
        maximum = "100",
        example = "85",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer matchPercent,
    @Schema(
        description = "Authoritative target-fit interpretation within the selected scoring configuration.",
        implementation = ProfileTargetFitStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetFitStatus fitStatus,
    @Schema(
        description = "Whether the point is inside the two-dimensional SG/TA target.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean insideSugarAcidityTarget,
    @Schema(
        description = "Whether the point is inside the full target, including optional tannin target.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean insideTarget,
    @Schema(description = "Nearest target point in SG/TA space.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetCoordinatesResponse nearestTargetPoint,
    @Schema(
        description = "Measured tannin, when available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse tannin,
    @Schema(
        description = "Nearest target tannin, when tannin target is configured.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse nearestTargetTannin,
    @Schema(
        description = "Objective function code.",
        implementation = ProfileTargetObjectiveFunctionCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(
        description = "Distance metric used by objective function.",
        implementation = ProfileDistanceMetric.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileDistanceMetric metric,
    @Schema(
        description = "Target kind.",
        implementation = SugarAcidityTargetKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityTargetKind targetKind,
    @Schema(
        description = "SG/TA target geometry shape.",
        implementation = SugarAcidityGeometryShape.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityGeometryShape targetShape,
    @ArraySchema(
        arraySchema = @Schema(description = "Missing profile axes.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ProfileEvaluationAxis.class)
    )
    @NonNull List<@NonNull ProfileEvaluationAxis> missingAxes
) {

}
