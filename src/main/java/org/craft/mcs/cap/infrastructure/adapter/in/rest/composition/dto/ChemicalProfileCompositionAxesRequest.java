package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Canonical chemical profile axes used as a composition source.")
public record ChemicalProfileCompositionAxesRequest(
    @Schema(description = "Sugar as specific gravity.", example = "1.060", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal sugar,
    @Schema(description = "Titratable acidity in g/L as malic acid.", example = "6.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal acidity,
    @Schema(description = "Tannin in mg/L TAE, when available.", example = "1200", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal tannin,
    @Schema(description = "Yeast-assimilable nitrogen in mg N/L, when available.", example = "180", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal yeastAssimilableNitrogen
) {
}
