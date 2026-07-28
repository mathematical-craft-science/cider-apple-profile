package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


@Schema(description = "Axis weights used by profile target objective functions.")
public record ProfileTargetObjectiveWeightsRequest(
    @Schema(
        description = "Specific Gravity (SG) axis weight.",
        example = "1.0",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonAlias("sugar")
    @Nullable BigDecimal specificGravity,
    @Schema(
        description = "Titratable Acidity (TA) axis weight.",
        example = "1.0",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonAlias("acidity")
    @Nullable BigDecimal titratableAcidity,
    @Schema(
        description = "Tannin axis weight.",
        example = "0.5",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal tannin
) {
}
