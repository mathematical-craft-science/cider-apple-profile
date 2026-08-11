package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.jspecify.annotations.NonNull;


/** Transport request for revision-checked replacement of editable vessel catalog data. */
@Schema(description = "Revision-checked replacement of editable vessel catalog data.")
public record UpdateVesselRequest(
    @Schema(
        description = "Workspace that owns the vessel.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID workspaceId,
    @Schema(
        description = "Revision returned by the latest vessel response.",
        format = "int64",
        example = "2",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @PositiveOrZero
    @NonNull Long expectedVersion,
    @Schema(
        description = "Workspace-unique vessel name.",
        example = "Fermentation tank F-03",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 160)
    @NonNull String name,
    @Schema(
        description = "Nominal vessel capacity in liters. Must be greater than zero and cannot be less than "
            + "the volume of current vessel content.",
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
    @NotNull @NonNull VesselMaterial material,
    @Schema(
        description = "Operational availability. OUT_OF_SERVICE and RETIRED require an empty vessel.",
        implementation = VesselOperationalStatus.class,
        example = "OPERATIONAL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull VesselOperationalStatus operationalStatus
) {
}
