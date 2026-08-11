package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.jspecify.annotations.NonNull;


/** Transport request for registering one workspace-owned physical vessel. */
@Schema(description = "Request to register a physical vessel in a workspace catalog.")
public record CreateVesselRequest(
    @Schema(
        description = "Workspace that owns the vessel.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID workspaceId,
    @Schema(
        description = "Workspace-unique vessel name.",
        example = "Fermentation tank F-03",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 160)
    @NonNull String name,
    @Schema(
        description = "Nominal vessel capacity in liters. Must be greater than zero.",
        example = "500.000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @DecimalMin(value = "0", inclusive = false)
    @NotNull @NonNull BigDecimal nominalCapacityLiters,
    @Schema(
        description = "Vessel construction material.",
        implementation = VesselMaterial.class,
        example = "STAINLESS_STEEL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull VesselMaterial material
) {
}
