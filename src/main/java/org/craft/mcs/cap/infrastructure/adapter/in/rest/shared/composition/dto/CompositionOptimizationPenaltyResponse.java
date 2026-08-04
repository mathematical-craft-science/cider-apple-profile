package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationPenaltyCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationPenaltySeverity;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Explainable penalty added to a composition optimization candidate score.")
public record CompositionOptimizationPenaltyResponse(
    @Schema(
        description = "Penalty code.",
        implementation = CompositionOptimizationPenaltyCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationPenaltyCode code,
    @Schema(
        description = "Penalty severity.",
        implementation = CompositionOptimizationPenaltySeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationPenaltySeverity severity,
    @Schema(
        description = "Penalty value added to objective score.",
        example = "0.250",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal value,
    @Schema(
        description = "Material characteristic assessed by the criterion.",
        implementation = MeasurementCharacteristic.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable MeasurementCharacteristic characteristic,
    @Schema(
        description = "Actual characteristic value in CAP canonical units, when available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse actualValue,
    @Schema(
        description = "Criterion threshold in CAP canonical units, when available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse threshold
) {
}
