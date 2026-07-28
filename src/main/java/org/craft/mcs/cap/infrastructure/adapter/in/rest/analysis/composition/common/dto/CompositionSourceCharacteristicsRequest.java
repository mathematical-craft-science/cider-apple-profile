package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Canonical source characteristics accepted by composition scenarios. */
@Schema(description = "Canonical source characteristics used in a composition.")
public record CompositionSourceCharacteristicsRequest(
    @Schema(
        description = "Specific Gravity (SG).",
        example = "1.060",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonAlias("sugar")
    @NonNull BigDecimal specificGravity,
    @Schema(
        description = "Titratable Acidity (TA) in g/L as malic acid.",
        example = "6.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonAlias("acidity")
    @NonNull BigDecimal titratableAcidity,
    @Schema(
        description = "Tannin in mg/L TAE, when available.",
        example = "1200",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen in mg N/L, when available.",
        example = "180",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal yeastAssimilableNitrogen
) {
}
