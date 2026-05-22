package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessFactor;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FermentationStartReadinessCheckResponse(
    @Schema(implementation = FermentationStartReadinessFactor.class)
    @NonNull FermentationStartReadinessFactor factor,
    @Schema(implementation = FermentationStartReadinessSeverity.class)
    @NonNull FermentationStartReadinessSeverity severity,
    @NonNull String code,
    @NonNull String message,
    @Nullable BigDecimal value,
    @Nullable String unit
) {
}
