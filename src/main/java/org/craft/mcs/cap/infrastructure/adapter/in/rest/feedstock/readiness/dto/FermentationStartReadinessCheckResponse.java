package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheckCode;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessFactor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single fermentation-start readiness check.")
public record FermentationStartReadinessCheckResponse(
    @Schema(
        description = "Checked factor.",
        implementation = FermentationStartReadinessFactor.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FermentationStartReadinessFactor factor,
    @Schema(
        description = "Check severity.",
        implementation = FermentationStartReadinessSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FermentationStartReadinessSeverity severity,
    @Schema(
        description = "Stable machine-readable check code.",
        implementation = FermentationStartReadinessCheckCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FermentationStartReadinessCheckCode code,
    @Schema(description = "Human-readable check message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message,
    @Schema(
        description = "Measured or derived quantity used by the check.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse value,
    @Schema(
        description = "Corrective action that currently explains a managed risk.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String managedByAction
) {
}
