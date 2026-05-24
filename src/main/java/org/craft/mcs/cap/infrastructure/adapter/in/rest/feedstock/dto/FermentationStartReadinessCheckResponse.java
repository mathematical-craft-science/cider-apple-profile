package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessFactor;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single fermentation-start readiness check.")
public record FermentationStartReadinessCheckResponse(
    @Schema(description = "Checked factor.", implementation = FermentationStartReadinessFactor.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FermentationStartReadinessFactor factor,
    @Schema(description = "Check severity.", implementation = FermentationStartReadinessSeverity.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FermentationStartReadinessSeverity severity,
    @Schema(description = "Machine-readable check code.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String code,
    @Schema(description = "Human-readable check message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message,
    @Schema(description = "Measured or derived value used by the check.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal value,
    @Schema(description = "Value unit label.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String unit
) {
}
