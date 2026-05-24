package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Accepted production profile snapshot in canonical service units.")
public record ProductionProfileSnapshotRequest(
    @Schema(description = "Sugar as specific gravity.", example = "1.052", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal sugar,
    @Schema(description = "Titratable acidity in g/L as malic acid.", example = "6.2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal acidity,
    @Schema(description = "Tannin in mg/L TAE, when available.", example = "1200", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal tannin
) {
}
